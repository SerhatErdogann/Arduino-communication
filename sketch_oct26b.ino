#include <ArduinoQueue.h>

ArduinoQueue<long> blinkQueue(80); 

int ledPin = 13;
bool blinkMode = false;
long blinkCounter = 0;
long blinkTarget=0;

String komut;
unsigned long previousMillis = 0;
bool ledState = LOW;

void setup() {
  Serial.begin(9600);
  pinMode(ledPin, OUTPUT);
}

void loop() {
  if (Serial.available()) {
    komut = Serial.readStringUntil('\n');
    komut.trim();

    if (komut == "LED:ON") {
      blinkMode=false;
      blinkTarget = 0;        
      blinkCounter = 0;       
      while (!blinkQueue.isEmpty()) {
        blinkQueue.dequeue();
      }
      digitalWrite(ledPin, HIGH);
      Serial.println("LED yakıldı.");
    }

    else if (komut == "LED:OFF") {
      blinkMode=false;
      blinkTarget = 0;        
      blinkCounter = 0; 
      while (!blinkQueue.isEmpty()) {
        blinkQueue.dequeue();
      }
      digitalWrite(ledPin, LOW);
      Serial.println("LED kapatıldı.");
    }

    else if (komut.startsWith("BLINK:")) {
     
     //toInt yapamadık cunku long olarak tanımlandı uzun bir değer gelme ihtimaline karşı
      long value = atol(komut.substring(6).c_str());
      if (value > 10000000) value = 10000000;  

      Serial.print("LED ");
      Serial.print(value);
      Serial.println(" kere yakılacak.");
      blinkQueue.enqueue(value);
      blinkMode = true;

   
}

  }

 if (blinkMode) {

if (blinkTarget == 0 && !blinkQueue.isEmpty()) {
  blinkTarget = blinkQueue.dequeue();
  blinkCounter = 0;
  
}

if (blinkTarget > 0) {

  //millis() metodu kaç milisaniye geçtiğine bakıyor
  unsigned long currentMillis = millis();
  if (currentMillis - previousMillis >= 200) {
    previousMillis = currentMillis;
    ledState = !ledState;
    digitalWrite(ledPin, ledState);

    if (!ledState) {
      blinkCounter++;

      if (blinkCounter >= blinkTarget) {
        blinkTarget = 0;
        blinkCounter = 0;
      }
    }
  }
}

  }

}


