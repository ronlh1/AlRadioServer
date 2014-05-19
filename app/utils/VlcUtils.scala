package utils

import org.apache.commons.exec.CommandLine
import java.net.Socket
import java.io.PrintWriter
import java.io.BufferedReader
import java.io.InputStreamReader
import org.apache.commons.exec._

object VlcUtils {

    /**
     * Call like "startVlcStreamServer('localhost', 5150, 'wgn.pls')"
     * plsFile - like "wgn.pls", "104_3.pls", etc. 
     */
    def buildVlcStreamServerCommand(host: String, port: Int, plsFile: String): CommandLine = {      
        if (runningOnMac) {
            buildVlcServerCommandMac(plsFile, host, port)
        } else {
            buildVlcServerCommandRpi(plsFile, host, port)
        }
    }
    
    def sendServerShutdownCommand(host: String, port: Int) {
        writeVlcCommandToSocket(host, port, "shutdown")
    }

    def increaseVolume(host: String, port: Int) {
        writeVlcCommandToSocket(host, port, "volup")
    }

    def increaseVolume(host: String, port: Int, amount: Int) {
        writeVlcCommandToSocket(host, port, s"volume $amount")
    }

    def decreaseVolume(host: String, port: Int) {
        writeVlcCommandToSocket(host, port, "voldown")
    }

    def decreaseVolume(host: String, port: Int, amount: Int) {
        writeVlcCommandToSocket(host, port, s"volume $amount")
    }

    /**
     * Amount can be "+10", "-10", etc.
     * TODO I think the units value is "seconds".
     */
    def seek(host: String, port: Int, amount: String) { writeVlcCommandToSocket(host, port, s"seek $amount") }
    def pause(host: String, port: Int)    { writeVlcCommandToSocket(host, port, "pause") }
    def play(host: String, port: Int)     { writeVlcCommandToSocket(host, port, "play") }
    def shutdown(host: String, port: Int) { writeVlcCommandToSocket(host, port, "shutdown") }

    /**
     * Sends the given command to the port the VLC server is running on.
     * Valid commands are "shutdown", "pause", "play", "seek +10", "voldown",
     * "volup", "volume +10".
     */
    private def writeVlcCommandToSocket(host: String, port: Int, command: String) {
        val socket = new Socket(host, port)
        val out = new PrintWriter(socket.getOutputStream, true)
        val in = new BufferedReader(new InputStreamReader(socket.getInputStream))
        out.println(command)
        socket.close
    }
        
    private def buildVlcServerCommandMac(fileOrStream: String, host: String, port: Int) = {
        val cmd = new CommandLine(getVlcServerCommand)
        cmd.addArgument(fileOrStream)
        cmd.addArgument("--rc-host")
        cmd.addArgument(s"${host}:${port}")
        cmd.addArgument("-I")
        cmd.addArgument("dummy")
        cmd.addArgument("-I")
        cmd.addArgument("rc")
        cmd
    }
    
    private def buildVlcServerCommandRpi(fileOrStream: String, host: String, port: Int) = {
        val cmd = new CommandLine(getVlcServerCommand)
        cmd.addArgument(fileOrStream)
        cmd.addArgument("--rc-host")
        cmd.addArgument(s"${host}:${port}")
        cmd.addArgument("-I")
        cmd.addArgument("dummy")
        cmd.addArgument("-I")
        cmd.addArgument("rc")
        cmd
    }
    
    private def getVlcServerCommand: String = {      
        if (runningOnMac) {
            "/Applications/VLC.app/Contents/MacOS/VLC"
        } else {
            "vlc"
        }
    }
    
    private def runningOnMac = {
        val osNameExists = System.getProperty("os.name").startsWith("Mac OS")
        val runningOnMac = if (!osNameExists) false else true
        runningOnMac
    }

    def executeCommand(cmd: CommandLine) {
        val watchDog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT)
    
        // result handler - exec the process in an async way
        val resultHandler = new DefaultExecuteResultHandler
    
        // use stdout for the output/error stream
        val streamHandler = new PumpStreamHandler
    
        // used to end the process when the jvm exits
        val processDestroyer = new ShutdownHookProcessDestroyer
    
        // main command executor
        val executor = new DefaultExecutor
    
        // properties
        executor.setStreamHandler(streamHandler)
        executor.setWatchdog(watchDog)
        executor.setProcessDestroyer(processDestroyer)
        
        // exec the command (async)
        executor.execute(cmd, resultHandler)
        
        // TODO i need to understand the code below better
    
//        // execution stops with this statement (until the command returns)
//        resultHandler.waitFor
//        println("*** immediately after 'waitFor' ***")
//    
//        val exitValue = resultHandler.getExitValue
//        println(exitValue)
    
//        if (executor.isFailure(exitValue)) {
//            System.out.println("Execution failed")
//        } else {
//            System.out.println("Execution Successful")
//        }

   }
    
}