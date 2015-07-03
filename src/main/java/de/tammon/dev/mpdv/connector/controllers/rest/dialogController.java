package de.tammon.dev.mpdv.connector.controllers.rest;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.UUID;

/**
 * Created by tammschw on 16/06/15.
 */
@RestController
@Scope("prototype")
@RequestMapping(value = "/api/")
public class dialogController {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${mpdv.hydra.host.name}")
    String hostName;

    @Value("${mpdv.hydra.host.user}")
    String userNumber;

    @Value("${server.port}")
    String portNumber;

    @RequestMapping(params = "dlg", method = RequestMethod.GET)
    public ResponseEntity<String> getResponseByDlg(String dlg) {
        Path outputFile = FileSystems.getDefault().getPath("result-" + UUID.randomUUID().toString() + ".out");
        Path executionFile = FileSystems.getDefault().getPath("ddcomtst.exe");

        String result;

        // create cmd command for executing ddcomtst.exe
        String line = executionFile.toString() + " " + hostName + " 3" + userNumber + " \"DLG=" + dlg + "\" " + outputFile.toString();
        CommandLine cmdLine = CommandLine.parse(line);
        DefaultExecutor executor = new DefaultExecutor();
        // Watchdog will kill Execute Watchdog after 15s of not reacting
        ExecuteWatchdog watchdog = new ExecuteWatchdog(15000);
        executor.setWatchdog(watchdog);

        try {
            // execute command
            int exitValue = executor.execute(cmdLine);
            result = readFile(outputFile, Charset.defaultCharset());

            // delete the result junk from working directory
            try {
                Files.delete(outputFile);
            } catch (NoSuchFileException x) {
                logger.error("%s: no such" + " file or directory%n", outputFile);
            } catch (DirectoryNotEmptyException x) {
                logger.error("%s not empty%n", outputFile);
            } catch (IOException x) {
                // File permission problems are caught here.
                logger.error(x.toString());
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ResponseEntity<>("There was an error during Hydra Communication. Please try again!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping("test")
    public ResponseEntity<String> test() {
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject("http://" + hostName + ":" + portNumber + "/api/?dlg={DLG}", String.class, "LIST");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(params = "DLG")
    public ResponseEntity<String> getResponseByDLG(String DLG) {
        return getResponseByDlg(DLG);
    }

    static String readFile(Path path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(path);
        return new String(encoded, encoding);
    }
}