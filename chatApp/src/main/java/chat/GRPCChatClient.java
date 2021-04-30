package chat;

import io.grpc.*;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.mark.grpc.grpcChat.ChatMessage;
import io.mark.grpc.grpcChat.ChatServiceGrpc;
import io.mark.grpc.grpcChat.ChatServiceGrpc.*;

public class GRPCChatClient {
  private ManagedChannel channel;
  private ChatServiceStub asyncStub;

  public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

  public GRPCChatClient(String host, int port) {
    this(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
  }

  public GRPCChatClient(ManagedChannelBuilder<?> channelBuilder) {
    channel = channelBuilder.build();
    asyncStub = ChatServiceGrpc.newStub(channel);
  }

  public static boolean validate(String emailStr) {
    Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
    return matcher.find();
  }

  public void chat(String username) {
    System.out.println("Insira suas mensagens: ");

    Metadata header = new Metadata();
    Metadata.Key<String> key =
        Metadata.Key.of("username", Metadata.ASCII_STRING_MARSHALLER);
    header.put(key, username);
    asyncStub = MetadataUtils.attachHeaders(asyncStub, header);
    final CountDownLatch finishLatch = new CountDownLatch(1);
    StreamObserver<ChatMessage> requestObserver =
        asyncStub.chat(new StreamObserver<ChatMessage>() {
          @Override
          public void onNext(ChatMessage res) {
            System.out.println(res.getFrom() + " ==> " + res.getMessage());
          }

          @Override
          public void onError(Throwable t) {
            Status status = Status.fromThrowable(t);
            System.err.println("Falha {0}" + status);
            finishLatch.countDown();
          }

          @Override
          public void onCompleted() {
            System.out.println("END");
            finishLatch.countDown();
          }
        });
    try {
      InputStream is = System.in;
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      String line = null;
      while ((line = br.readLine()) != null) {
        if (line.equalsIgnoreCase("q")) {
          break;
        }
        requestObserver.onNext(ChatMessage.newBuilder().setMessage(line).build());
      }
    } catch (Exception e) {
      System.out.println("Exception occured:" + e);
      requestObserver.onError(e);

    }
  }

  public static void main(String[] args) {
    GRPCChatClient grpcChatClient = new GRPCChatClient("localhost", 50050);
    if (!grpcChatClient.validate(System.getProperty("username"))) {
      System.out.println("Email inválido. Tente novamente");
      return;
    } else {
      System.out.println("Bem vindo, " + (System.getProperty("username")));
      grpcChatClient.chat(System.getProperty("username"));
    }

  }
}
//mvn package
// java -cp target/chatApp-1.0-SNAPSHOT.jar -Dusername=romulo_pereira@email.com chat.GRPCChatClient