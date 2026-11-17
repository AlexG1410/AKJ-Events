import test from "node:test";
import assert from "node:assert/strict";
import { fileURLToPath } from "node:url";
import { Miniflare, createFetchMock } from "miniflare";

test("workerd supports both detached fetch and the receiver-preserving wrapper", async () => {
  const outbound = createFetchMock();
  outbound.disableNetConnect();
  outbound.get("https://probe.test").intercept({ path: "/" }).reply(200, "ok").times(2);
  const mf = new Miniflare({ modules: true, compatibilityDate: "2026-07-30", fetchMock: outbound,
    script: `const detached = globalThis.fetch;
      export default { async fetch() {
        let failure;
        try { await detached("https://probe.test/"); } catch (error) { failure = { name: error.name, message: error.message }; }
        const response = await ((...args) => globalThis.fetch(...args))("https://probe.test/");
        return Response.json({ failure, wrappedStatus: response.status });
      } };`,
  });
  try {
    const result = await (await mf.dispatchFetch("https://worker.test/")).json();
    assert.equal(result.failure, undefined);
    assert.equal(result.wrappedStatus, 200);
    outbound.assertNoPendingInterceptors();
  } finally { await mf.dispose(); await outbound.close(); }
});

test("workerd supports AbortSignal.timeout used by the outbound requests", async () => {
  const mf = new Miniflare({ modules: true, compatibilityDate: "2026-07-30", script: `
    export default { fetch() {
      try { const signal = AbortSignal.timeout(5000); return Response.json({ supported: true, aborted: signal.aborted }); }
      catch (error) { return Response.json({ supported: false, name: error.name, message: error.message }); }
    } };` });
  try {
    const result = await (await mf.dispatchFetch("https://worker.test/")).json();
    assert.equal(result.supported, true, JSON.stringify(result));
  } finally { await mf.dispose(); }
});

test("workerd rejects legacy redirect:error but supports manual without following redirects", async () => {
  const outbound = createFetchMock();
  outbound.disableNetConnect();
  outbound.get("https://probe.test").intercept({ path: "/" }).reply(200, "ok");
  const mf = new Miniflare({ modules: true, compatibilityDate: "2026-07-30", fetchMock: outbound, script: `
    export default { async fetch() {
      let legacy;
      try { await fetch("https://probe.test/", { redirect: "error" }); }
      catch (error) { legacy = { name: error.name, message: error.message }; }
      const response = await fetch("https://probe.test/", { redirect: "manual", signal: AbortSignal.timeout(5000) });
      return Response.json({ legacy, status: response.status });
    } };` });
  try {
    const result = await (await mf.dispatchFetch("https://worker.test/")).json();
    assert.equal(result.status, 200, JSON.stringify(result));
    assert.equal(result.legacy.name, "TypeError");
    assert.match(result.legacy.message, /Invalid redirect value/);
  } finally { await mf.dispose(); await outbound.close(); }
});

for (const scenario of ["category", "recommendations", "outer-and-inner-bom", "incomplete-generation"]) {
test(`actual workerd runtime: ${scenario}`, async () => {
  const pair = await crypto.subtle.generateKey({ name: "RSASSA-PKCS1-v1_5", modulusLength: 2048, publicExponent: new Uint8Array([1, 0, 1]), hash: "SHA-256" }, true, ["sign", "verify"]);
  const jwk = { ...await crypto.subtle.exportKey("jwk", pair.publicKey), kid: "runtime-test-key", alg: "RS256" };
  const seconds = Math.floor(Date.now() / 1000);
  const encode = (data) => Buffer.from(JSON.stringify(data)).toString("base64url");
  const unsigned = `${encode({ alg: "RS256", kid: jwk.kid })}.${encode({ aud: "demo-akj-events", iss: "https://securetoken.google.com/demo-akj-events", sub: "runtime-user", iat: seconds - 10, auth_time: seconds - 20, exp: seconds + 3600 })}`;
  const signature = await crypto.subtle.sign("RSASSA-PKCS1-v1_5", pair.privateKey, new TextEncoder().encode(unsigned));
  const token = `${unsigned}.${Buffer.from(signature).toString("base64url")}`;
  const outbound = createFetchMock();
  outbound.disableNetConnect();
  let calls = 0;
  outbound.get("https://www.googleapis.com").intercept({ path: "/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com" })
    .reply(200, { keys: [jwk] }, { headers: { "Content-Type": "application/json", "Cache-Control": "max-age=3600" } });
  outbound.get("https://openrouter.ai").intercept({ path: "/api/v1/chat/completions", method: "POST" })
    .reply(200, () => {
      calls++;
      const content = scenario === "recommendations" ? '{"items":[{"eventId":"a","reason":"Por tus intereses"}]}' : '{"category":"ACADEMIC"}';
      return (scenario === "outer-and-inner-bom" ? '\uFEFF' : '') + JSON.stringify({ model: "test/free-model", choices: [{ finish_reason: scenario === "incomplete-generation" ? "length" : "stop", message: { content: (scenario === "outer-and-inner-bom" ? '\uFEFF' : '') + content } }] });
    }, { headers: { "Content-Type": "application/json" } });
  const mf = new Miniflare({
    modules: true,
    scriptPath: fileURLToPath(new URL("../src/index.js", import.meta.url)),
    compatibilityDate: "2026-07-30",
    bindings: { FIREBASE_PROJECT_ID: "demo-akj-events", OPENROUTER_API_KEY: "fake-runtime-secret" },
    fetchMock: outbound,
  });
  try {
    const response = await mf.dispatchFetch(`https://worker.test/${scenario === "recommendations" ? "recommend-events" : "suggest-category"}`, {
      method: "POST", headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
      body: JSON.stringify(scenario === "recommendations" ? {
        interests: ["ACADEMIC"], interactions: [], language: "es",
        candidates: [{ eventId: "a", title: "Taller comunitario", description: "Aprenderemos juntos sobre huertos urbanos.", category: "ACADEMIC" }],
      } : { title: "Taller comunitario", description: "Aprenderemos juntos sobre huertos urbanos." }),
    });
    const result = await response.json();
    assert.equal(response.status, scenario === "incomplete-generation" ? 502 : 200, JSON.stringify(result));
    assert.deepEqual(result, scenario === "incomplete-generation" ? { error: "invalid_ai_response" } : scenario === "recommendations" ? { items: [{ eventId: "a", reason: "Por tus intereses" }] } : { category: "ACADEMIC" });
    assert.equal(calls, 1);
    outbound.assertNoPendingInterceptors();
  } finally {
    await mf.dispose();
    await outbound.close();
  }
});
}
