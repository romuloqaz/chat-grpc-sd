cd chatApp

mvn clean

mvn package

java -cp target/chatApp-1.0-SNAPSHOT.jar -Dusername=usuario_java@email.com chat.GRPCChatClient
