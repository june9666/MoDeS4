package hu.bme.ftsrg.modes4.mqtt2sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;







public abstract class Data2SQL
  implements MqttCallback
{
  private static final Boolean info = Boolean.valueOf(true);
  
  private final String topic;
  
  private final String mqttBrokerURL = "tcp://192.168.1.150:1883";
  private String clientID = "MQTT2SQL";
  

  MqttClient client;
  

  final String jdbcDriver = "com.mysql.cj.jdbc.Driver";
  final String dbURL = "jdbc:mysql://localhost:3306/iot_db";
  final String dbUsername = "gateway3";
  final String dbPassword = "new_password";
  Connection dbConnection = null;
  Statement dbStatement = null;
  
  protected abstract String getSQL(String paramString);
  
  public Data2SQL(String topic_) {
    topic = topic_;
    clientID += topic;
  }
  
  public void connect2Broker() throws MqttException {
    client = new MqttClient(mqttBrokerURL, clientID);
    client.connect();
    client.setCallback(this);
    client.subscribe(topic);
  }
  
  public void connect2Database() throws Exception
  {
    Class.forName("com.mysql.cj.jdbc.Driver");
    dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/iot_db", "gateway3", "new_password");
    dbStatement = dbConnection.createStatement();
  }
  



  public void messageArrived(String topic, MqttMessage message)
  {
    System.out.println("mew mqtt message has arrived into topic:" + this.topic);
    try {
      if (dbConnection.isClosed()) {
        dbStatement.close();
        dbConnection.close();
        return;
      }
      int rs = dbStatement.executeUpdate(getSQL(message.toString()));
      if (info.booleanValue()) {
        System.out.println(message);
        System.out.println(rs);
      }
    }
    catch (Exception e)
    {
      if (info.booleanValue()) {
        System.out.println(e.toString());
      }
    }
  }
}