#!/bin/sh
# Compila e roda o Caça-Bugs (Linux, macOS ou WSL). Requer JDK 17 ou mais novo.
# Se aparecerem símbolos estranhos no labirinto, rode:  ./executar.sh --ascii
set -e
cd "$(dirname "$0")"
rm -rf out
mkdir -p out
javac --release 17 -encoding UTF-8 -d out -sourcepath src src/cacabugs/Jogo.java
cp -r src/recursos out/
java -cp out cacabugs.Jogo "$@"
