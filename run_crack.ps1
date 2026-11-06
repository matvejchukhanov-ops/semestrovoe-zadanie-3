# ============================================================
#  Запуск.ps1 — восстановление паролей по хэшам задания №3
#  Перебирает тестовые хэши (MD5/SHA-1/bcrypt/Argon2) словарём
#  через hashcat и складывает найденные пароли в отчёт-блокнот
#  "Найденные_пароли.txt" рядом с этим файлом.
# ============================================================
param([switch]$NoOpen)   # -NoOpen — не открывать блокнот (для авто-проверки)

$ErrorActionPreference = 'Continue'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 > $null

$Root       = $PSScriptRoot
$Report     = Join-Path $Root 'Найденные_пароли.txt'
$HashcatDir = 'C:\Claude pr\tools\hashcat\hashcat-8.0.0'
$Hashcat    = Join-Path $HashcatDir 'hashcat.exe'
$Work       = Join-Path $env:TEMP 'sem3_crack'
New-Item -ItemType Directory -Force $Work | Out-Null

# ---- Тестовые хэши задания (алгоритм, режим hashcat, сложность, хэш) ----
$items = @(
  @{a='MD5';     m=0;     d='лёгкий';        h='e10adc3949ba59abbe56e057f20f883e'}
  @{a='MD5';     m=0;     d='средний';       h='1f3870be274f6c49b3e31a0c6728957f'}
  @{a='MD5';     m=0;     d='сложный';       h='77892341aa9dc66e97f5c248782b5d92'}
  @{a='MD5';     m=0;     d='очень сложный'; h='686e697538050e4664636337cc3b834f'}
  @{a='SHA-1';   m=100;   d='лёгкий';        h='7c4a8d09ca3762af61e59520943dc26494f8941b'}
  @{a='SHA-1';   m=100;   d='средний';       h='d0be2dc421be4fcd0172e5afceea3970e2f3d940'}
  @{a='SHA-1';   m=100;   d='сложный';       h='666846867fc5e0a46a7afc53eb8060967862f333'}
  @{a='SHA-1';   m=100;   d='очень сложный'; h='6e157c5da4410b7e9de85f5c93026b9176e69064'}
  @{a='bcrypt';  m=3200;  d='лёгкий';        h='$2a$10$z4u9ZkvopUiiytaNX7wfGedy9Lu2ywUxwYpbsAR5YBrAuUs3YGXdi'}
  @{a='bcrypt';  m=3200;  d='средний';       h='$2a$10$26GB/T2/6aTsMkTjCgqm/.JP8SUjr32Bhfn9m9smtDiIwM4QIt2ze'}
  @{a='bcrypt';  m=3200;  d='сложный';       h='$2a$10$Q9M0vLLrE4/nu/9JEMXFTewB3Yr9uMdIEZ1Sgdk1NQTjHwLN0asfi'}
  @{a='bcrypt';  m=3200;  d='очень сложный'; h='$2a$10$yZBadi8Szw0nItV2g96P6eqctI2kbG/.mb0uD/ID9tlof0zpJLLL2'}
  @{a='Argon2id';m=34000; d='лёгкий';        h='$argon2id$v=19$m=65536,t=3,p=2$c2FsdHNhbHQ$PUF5UxxoUY++mMekkQwFurL0ZsTtB7lelO23zcyZQ0c'}
  @{a='Argon2id';m=34000; d='средний';       h='$argon2id$v=19$m=65536,t=3,p=2$c2FsdHNhbHQ$HYQwRUw9VcfkvqkUQ5ppyYPom6f/ro3ZCXYznhrYZw4'}
  @{a='Argon2id';m=34000; d='сложный';       h='$argon2id$v=19$m=65536,t=3,p=2$c2FsdHNhbHQ$9asGA7Xv3vQBz7Yyh4/Ntw0GQgOg8R6OWolOfRETrEg'}
  @{a='Argon2id';m=34000; d='очень сложный'; h='$argon2id$v=19$m=65536,t=3,p=2$c2FsdHNhbHQ$+smq45/czydGj0lYNdZVXF++FOXJwrkXt6VUIcEauvo'}
)

# ---- Словарь типовых паролей ----
$wl = Join-Path $Work 'wordlist.txt'
@('123456','apple','qwerty','password','12345678','abc123','letmein',
  'monkey','dragon','111111','iloveyou','admin','welcome','sunshine') |
  Set-Content -Encoding ascii $wl

if (-not (Test-Path $Hashcat)) {
  "hashcat не найден: $Hashcat`r`nПоложите hashcat 8.0 в указанную папку или поправьте путь в Запуск.ps1." |
    Set-Content -Encoding UTF8 $Report
  if (-not $NoOpen) { Start-Process notepad $Report }
  return
}

$found = @{}   # хэш -> пароль

function Crack-File($mode, $hashFile) {
  $out = Join-Path $Work ("cracked_{0}_{1}.txt" -f $mode, ([IO.Path]::GetFileNameWithoutExtension($hashFile)))
  Remove-Item $out -EA SilentlyContinue
  Push-Location $HashcatDir
  & $Hashcat -m $mode -a 0 -d 1 --potfile-disable --self-test-disable --quiet -o $out $hashFile $wl 2>$null | Out-Null
  Pop-Location
  if (Test-Path $out) {
    foreach ($line in Get-Content $out) {
      if ($line -match '^(.*):([^:]*)$') { $found[$matches[1]] = $matches[2] }
    }
  }
}

# MD5 / SHA-1 / bcrypt — все хэши одного алгоритма в одном файле
foreach ($grp in @(@{a='MD5';m=0}, @{a='SHA-1';m=100}, @{a='bcrypt';m=3200})) {
  Write-Host ("Перебор {0}..." -f $grp.a) -ForegroundColor Cyan
  $f = Join-Path $Work ("in_{0}.txt" -f $grp.m)
  ($items | Where-Object { $_.a -eq $grp.a } | ForEach-Object { $_.h }) | Set-Content -Encoding ascii $f
  Crack-File $grp.m $f
}

# Argon2id — по одному хэшу на запуск (общая соль у всех четырёх)
Write-Host "Перебор Argon2id (GPU)..." -ForegroundColor Cyan
$ai = 0
foreach ($it in ($items | Where-Object { $_.a -eq 'Argon2id' })) {
  $ai++
  $f = Join-Path $Work ("in_argon_{0}.txt" -f $ai)
  $it.h | Set-Content -Encoding ascii $f
  Crack-File 34000 $f
}

# ---- Формирование отчёта ----
$lines = @()
$lines += '============================================================'
$lines += '  ОТЧЁТ: восстановленные пароли'
$lines += '  Семестровое задание №3 — алгоритмы хэширования'
$lines += ('  Дата: {0}' -f (Get-Date -Format 'dd.MM.yyyy HH:mm'))
$lines += '  Инструмент: hashcat 8.0.0 (словарный перебор, GPU)'
$lines += '============================================================'
$lines += ''

$total = 0
foreach ($algo in @('MD5','SHA-1','bcrypt','Argon2id')) {
  $lines += ("[{0}]" -f $algo)
  foreach ($it in ($items | Where-Object { $_.a -eq $algo })) {
    $pw = $found[$it.h]
    if ($pw) {
      $total++
      $lines += ('  {0,-14} ПАРОЛЬ: {1,-12} (хэш {2}...)' -f $it.d, $pw, $it.h.Substring(0, [Math]::Min(16, $it.h.Length)))
    } else {
      $lines += ('  {0,-14} — не найден   (хэш {1}...)' -f $it.d, $it.h.Substring(0, [Math]::Min(16, $it.h.Length)))
    }
  }
  $lines += ''
}
$lines += '------------------------------------------------------------'
$lines += ("ИТОГО восстановлено: {0} из {1}" -f $total, $items.Count)
$lines += 'Примечание: «сложные» и «очень сложные» пароли на bcrypt/Argon2'
$lines += 'словарём не берутся — это и есть доказательство стойкости.'

$lines -join "`r`n" | Set-Content -Encoding UTF8 $Report
Write-Host ("Готово. Найдено {0} из {1}. Отчёт: {2}" -f $total, $items.Count, $Report) -ForegroundColor Green

if (-not $NoOpen) { Start-Process notepad $Report }
