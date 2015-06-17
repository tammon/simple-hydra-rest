package de.tammon.dev.mpdv.connector.controllers.rest;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by tammschw on 16/06/15.
 */
@RestController
@RequestMapping(value = "/api/")
public class dialogController {

    @Value("${mpdv.folder.prefix}")
    String folderPrefix;

    @Value("${mpdv.hydra.host.name}")
    String hostName;

    @Value("${mpdv.hydra.host.user}")
    String userNumber;

    @RequestMapping(params = "dlg", method = RequestMethod.GET)
    public ResponseEntity<String> getResponseByDlg(String dlg) {
//        dlg = dlg.replaceAll("[|]","^|");
//        dlg = dlg.replaceAll("[;]","^;");
//        dlg = dlg.replaceAll("[=]","^=");
//        String line = "C:\\hydraExeConnector\\dlg.bat \"DLG=" + dlg + "\"";
        String line = folderPrefix + "ddcomtst.exe " + hostName + " 3" + userNumber + " \"DLG=" + dlg + "\" " + folderPrefix + "result.txt";
        String result;
        CommandLine cmdLine = CommandLine.parse(line);
        DefaultExecutor executor = new DefaultExecutor();
        ExecuteWatchdog watchdog = new ExecuteWatchdog(15000);
        executor.setWatchdog(watchdog);
        try {
            int exitValue = executor.execute(cmdLine);
            result = readFile(folderPrefix + "result.txt", Charset.defaultCharset());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
