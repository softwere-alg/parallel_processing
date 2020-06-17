@echo off

cd src

javac -cp ..\lib\jimObjModelImporterJFX.jar; -d ..\bin -encoding UTF-8 application\*.java
copy application\Main.fxml ..\bin\application
copy application\subWindow\SubWindow.fxml ..\bin\application\subWindow

cd ../