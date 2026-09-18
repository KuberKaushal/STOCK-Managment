@echo off
echo ===================================================
echo Running JUnit 5 Enterprise Test Suite...
echo ===================================================
if not exist bin mkdir bin
javac -d bin -cp "lib/junit-platform-console-standalone.jar;src" src/com/vityarthi/inventory/model/*.java src/com/vityarthi/inventory/exception/*.java src/com/vityarthi/inventory/util/*.java src/com/vityarthi/inventory/service/*.java src/com/vityarthi/inventory/*.java src/com/vityarthi/inventory/test/*.java
java -jar lib/junit-platform-console-standalone.jar execute --class-path bin --select-class com.vityarthi.inventory.test.StoreManagerTest
pause