#!/bin/sh
# Gera ../report.pdf a partir de report.md e ../bug-catcher.zip a partir de project-3.
# Requer pandoc, graphviz (dot), google-chrome e zip.
set -e
cd "$(dirname "$0")"

# Diagrama UML
dot -Tsvg uml.dot -o uml.svg

# Relatório
pandoc report.md -s --embed-resources --css style.css --metadata lang=pt-BR -o report.html
google-chrome --headless --disable-gpu --no-pdf-header-footer --print-to-pdf="$PWD/../report.pdf" "file://$PWD/report.html"

# Código-fonte para entrega (sem target/, logs e recordes)
TMP=$(mktemp -d)
mkdir "$TMP/bug-catcher"
cd ../../project-3
cp -r pom.xml mvnw mvnw.cmd .mvn src README.md .gitignore "$TMP/bug-catcher/"
cp ../submission/source/HOW_TO_RUN.md "$TMP/bug-catcher/"
rm -f ../submission/bug-catcher.zip
(cd "$TMP" && zip -qr - bug-catcher) > ../submission/bug-catcher.zip
rm -rf "$TMP"
echo "OK: submission/report.pdf e submission/bug-catcher.zip"
