package com.example.arduinosinyalgonderme;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.io.IOException;
import java.util.*;
import java.util.regex.*;

public class SignalController {

    @FXML
    private TextField textField;
    @FXML
    private TextArea textArea;

    private SerialPort port;

    @FXML
    private void initialize() {
        textArea.appendText("Arduino bağlantısı aranıyor...\n");

        Thread connectionThread = new Thread(() -> {
            while (true) {
                try {
                    if (port == null || !port.isOpen()) {
                        connectPortAndMessage();
                    }
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //uygulama kapandıgında thread de kapansın JVM açık kalmasın
        connectionThread.setDaemon(true);
        connectionThread.start();
    }

    private void connectPortAndMessage() {
        //bilgisayarın tüm usb girişlerini arraye attık
        SerialPort[] ports = SerialPort.getCommPorts();

        for (SerialPort p : ports) {
            String name = p.getDescriptivePortName().toLowerCase();
            if (name.contains("ch340") || name.contains("usb")) {
                try {
                    //mesaj gitme bit hizi(klasik)
                    p.setBaudRate(9600);

                    // port mesgul mu kontrol etmek için
                    if (p.openPort()) {
                        port = p;
                        //Background thread'inde olduğu için direk append olmuyor. JavaFX izin vermıyor yani.
                        Platform.runLater(() ->
                                textArea.appendText("Arduino'ya bağlanıldı: " + p.getSystemPortName() + "\n")
                        );

                        port.addDataListener(new SerialPortDataListener() {
                            @Override
                            public int getListeningEvents() {
                                //bağlantı koptuğunda tetiklenip serial evente atacak
                                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE | SerialPort.LISTENING_EVENT_PORT_DISCONNECTED;
                            }

                            @Override
                            public void serialEvent(SerialPortEvent event) {
                                if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                                    try {
                                        byte[] buffer = new byte[port.bytesAvailable()];
                                        int numRead = port.getInputStream().read(buffer);
                                        String gelenMesaj = new String(buffer, 0, numRead);

                                        // JavaFX GUI'de göstermek için
                                        Platform.runLater(() -> {
                                            textArea.appendText(gelenMesaj);
                                        });

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                else if (event.getEventType() == SerialPort.LISTENING_EVENT_PORT_DISCONNECTED) {
                                    Platform.runLater(() -> textArea.appendText("Bağlantı kapatıldı.\n"));
                                    disconnectPort();
                                }
                            }
                        });

                        return;
                    }
                } catch (Exception e) {
                    Platform.runLater(() ->
                            textArea.appendText("Bağlantı kurulamadı: " + e.getMessage() + "\n")
                    );
                }
            }
        }

        Platform.runLater(() ->
                textArea.appendText("Arduino bulunamadı. Yeniden deniyor...\n")
        );
    }

    private void disconnectPort() {
        if (port != null && port.isOpen()) {
            port.closePort();
        }
        port = null;
    }

    @FXML
    private void messageButton() {
        try {
            if (port == null || !port.isOpen()) {
                //Butonlar JavaFX Application Thread'inde olduğu için Platform.runlater'a gerek yok.
                textArea.appendText("Herhangi bir porta bağlanılmadı.\n");
                return;
            }

            String input = textField.getText().toLowerCase();
            long count = 0;

            Pattern pattern = Pattern.compile("/(\\d+)");
            Matcher matcher = pattern.matcher(input);

            while (matcher.find()) {
                //pattern içindeki regex de her parantez bloğu bir grup oluşturur. ayrımı yapmak için kullandık.
                try {
                    // Normal durumda long'a çevir
                    count += Long.parseLong(matcher.group(1));

                } catch (NumberFormatException ex) {
                    // Çok büyük sayı olursa eror atmaması ıcın
                    count = 10000000;
                }
            }

            inputYollama(count);

        } catch (Exception e) {
            e.printStackTrace();
        }
        textField.clear();

    }
    private void inputYollama(long count) throws IOException {
        if (count <= 0) {
            textArea.appendText("Uygun bir sayı girilmedi.\n");
            return;
        }
        port.getOutputStream().write(("BLINK:" + count + "\n").getBytes());
        port.getOutputStream().flush();

    }

    @FXML
    private void isigiAcButton() throws IOException {
        if(port == null ||!port.isOpen()) {
            textArea.appendText("Herhangi bir bağlantı yok.\n");
            return;
        }
        port.getOutputStream().write("LED:ON\n".getBytes());
        port.getOutputStream().flush();
    }

    @FXML
    private void isigiKapatButton()  throws IOException {
        if(port == null || !port.isOpen()) {
            textArea.appendText("Herhangi bir bağlantı yok.\n");
            return;
        }
        port.getOutputStream().write("LED:OFF\n".getBytes());
        port.getOutputStream().flush();

    }
}
