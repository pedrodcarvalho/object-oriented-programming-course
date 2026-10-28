@echo off
rem Compila e roda o Caca-Bugs no Windows. Requer JDK 17 ou mais novo.
rem No Windows cada comando precisa de ENTER depois da tecla; para jogar
rem com as teclas na hora, use o WSL e o executar.sh.
chcp 65001 > nul
cd /d "%~dp0"
if exist out rmdir /s /q out
mkdir out
javac --release 17 -encoding UTF-8 -d out -sourcepath src src\cacabugs\Jogo.java
if errorlevel 1 goto fim
xcopy /e /i /q /y src\recursos out\recursos > nul
java -cp out cacabugs.Jogo %*
:fim
