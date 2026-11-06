# ============================================================
#  Живая демонстрация — семестровое задание №3 (РБПО, МИРЭА)
#  Переборщик паролей + эталоны John the Ripper и hashcat.
#  Запуск:  правый клик по demo.bat -> запустить,
#           либо:  powershell -ExecutionPolicy Bypass -File demo.ps1
# ============================================================

$ErrorActionPreference = 'Continue'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 > $null

# --- Пути (ищем автоматически, иначе берём известные) ---
$RepoDir = $PSScriptRoot
$Java = (Get-Command java -EA SilentlyContinue).Source
if (-not $Java) { $Java = 'C:\Program Files\Microsoft\jdk-21.0.13.21-hotspot\bin\java.exe' }
$John = 'C:\Claude pr\tools\john\john-1.9.0-jumbo-1-win64\run\john.exe'
$HashcatDir = 'C:\Claude pr\tools\hashcat\hashcat-8.0.0'
$Hashes = 'C:\Claude pr\tools\hashes'
$CP = "$RepoDir\out;$RepoDir\lib\*"

function Pause-Step($title) {
    Write-Host ""
    Write-Host "============================================================" -ForegroundColor DarkGray
    Write-Host "  $title" -ForegroundColor Cyan
    Write-Host "============================================================" -ForegroundColor DarkGray
    Read-Host "  [Enter] — выполнить"
}
function Show-Cmd($text) { Write-Host "> $text" -ForegroundColor Yellow }

function Run-Mine($arguments) {
    & $Java "-Dstdout.encoding=UTF-8" "-cp" $CP "ru.mirea.bruteforce.Main" @arguments
}

Clear-Host
Write-Host "ДЕМОНСТРАЦИЯ: сравнение стойкости MD5 / SHA-1 / bcrypt / Argon2" -ForegroundColor Green
Write-Host "Java:    $Java"
Write-Host "Репо:    $RepoDir"
Write-Host "John:    $John"
Write-Host "hashcat: $HashcatDir"

# ---------- 1. Свой инструмент: справка ----------
Pause-Step "1/7  Свой инструмент — справка по командам"
Show-Cmd "java ... Main help"
Run-Mine @("help")

# ---------- 2. Хэширование ----------
Pause-Step "2/7  Вычисление хэша строки (команда hash)"
Show-Cmd "java ... Main hash --algo md5 --text 123456"
Run-Mine @("hash","--algo","md5","--text","123456")
Write-Host "(совпадает с лёгким MD5-хэшем из задания e10adc39...)" -ForegroundColor DarkGray

# ---------- 3. MD5 перебор ----------
Pause-Step "3/7  Подбор MD5 (быстрый хэш) — доли секунды"
Show-Cmd "java ... Main crack --algo md5 --hash e10adc3949ba59abbe56e057f20f883e --charset digits --max 8"
Run-Mine @("crack","--algo","md5","--hash","e10adc3949ba59abbe56e057f20f883e","--charset","digits","--max","8","--threads","8")

# ---------- 4. bcrypt перебор ----------
Pause-Step "4/7  Подбор bcrypt (адаптивный хэш) — десятки хэшей/с"
Write-Host "Сначала получим bcrypt-хэш пароля 'ab', затем подберём его:" -ForegroundColor DarkGray
$bc = (Run-Mine @("hash","--algo","bcrypt","--text","ab")) | Select-Object -Last 1
Show-Cmd "java ... Main crack --algo bcrypt --hash `"$bc`" --charset lower --max 2"
Run-Mine @("crack","--algo","bcrypt","--hash","$bc","--charset","lower","--max","2","--threads","8")
Write-Host "=> bcrypt в десятки тысяч раз медленнее MD5 при том же переборе." -ForegroundColor Green

# ---------- 5. Эталон John: MD5 / SHA-1 ----------
Pause-Step "5/7  Эталон John the Ripper — MD5 и SHA-1"
if (Test-Path $John) {
    Remove-Item "$(Split-Path $John)\john.pot" -EA SilentlyContinue
    Show-Cmd "john --format=raw-md5 --mask=?d?d?d?d?d?d md5.txt"
    & $John --format=raw-md5 --mask='?d?d?d?d?d?d' "$Hashes\md5.txt"
    Show-Cmd "john --format=raw-sha1 --wordlist --rules sha1.txt"
    & $John --format=raw-sha1 --wordlist --rules "$Hashes\sha1.txt"
} else { Write-Host "John не найден: $John" -ForegroundColor Red }

# ---------- 6. Эталон John: bcrypt ----------
Pause-Step "6/7  Эталон John the Ripper — bcrypt (16 потоков OpenMP)"
if (Test-Path $John) {
    Remove-Item "$(Split-Path $John)\john.pot" -EA SilentlyContinue
    Show-Cmd "john --format=bcrypt --wordlist --rules --max-run-time=30 bcrypt.txt"
    & $John --format=bcrypt --wordlist --rules --max-run-time=30 "$Hashes\bcrypt.txt"
}

# ---------- 7. Эталон hashcat: Argon2id на GPU ----------
Pause-Step "7/7  Эталон hashcat — Argon2id на GPU (RTX 3050)"
if (Test-Path "$HashcatDir\hashcat.exe") {
    Push-Location $HashcatDir
    Show-Cmd "hashcat -m 34000 -a 0 -d 1 a2\h1.txt wl_test.txt   # argon2id, лёгкий"
    & ".\hashcat.exe" -m 34000 -a 0 -d 1 --potfile-disable --self-test-disable "a2\h1.txt" "wl_test.txt"
    Show-Cmd "hashcat -m 34000 --show a2\h1.txt"
    & ".\hashcat.exe" -m 34000 --show "a2\h1.txt"
    Pop-Location
} else { Write-Host "hashcat не найден: $HashcatDir" -ForegroundColor Red }

Write-Host ""
Write-Host "ИТОГ: быстрые хэши (MD5/SHA-1) ломаются миллионами в секунду," -ForegroundColor Green
Write-Host "      bcrypt — сотнями, Argon2id — десятками даже на GPU." -ForegroundColor Green
Write-Host "Демонстрация завершена." -ForegroundColor Green
Read-Host "[Enter] — выход"
