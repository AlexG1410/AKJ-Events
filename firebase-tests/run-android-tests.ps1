$ErrorActionPreference = 'Stop'
if ($env:FIRESTORE_EMULATOR_HOST -ne '127.0.0.1:8180' -or $env:FIREBASE_AUTH_EMULATOR_HOST -ne '127.0.0.1:9199') {
    throw 'LOCAL_EMULATORS_REQUIRED'
}
if (-not $env:ANDROID_SERIAL -or $env:ANDROID_SERIAL -notlike 'emulator-*') {
    throw 'Set ANDROID_SERIAL to the Android emulator, not a production phone'
}
Push-Location (Split-Path $PSScriptRoot -Parent)
$sdk = if ($env:ANDROID_HOME) { $env:ANDROID_HOME } else { Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
$adb = Join-Path $sdk 'platform-tools\adb.exe'
try {
    & $adb -s $env:ANDROID_SERIAL reverse tcp:9199 tcp:9199
    & $adb -s $env:ANDROID_SERIAL reverse tcp:8180 tcp:8180
    & .\gradlew.bat :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=co.edu.uniquindio.akjevents.AccountDeletionEmulatorTest,co.edu.uniquindio.akjevents.DeleteAccountDialogSmokeTest' '-Pandroid.testInstrumentationRunnerArguments.localDeletionEmulators=true' --console=plain --max-workers=2
    exit $LASTEXITCODE
} finally {
    & $adb -s $env:ANDROID_SERIAL reverse --remove tcp:9199
    & $adb -s $env:ANDROID_SERIAL reverse --remove tcp:8180
    Pop-Location
}
