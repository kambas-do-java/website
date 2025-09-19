@echo off
setlocal

echo Gerando site estático...

echo Executando gerador...
java --enable-preview --source 24 main.java

echo Site gerado em site/
pause
