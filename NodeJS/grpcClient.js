var PROTO_PATH = __dirname + '/grpc_chat.proto';
var grpc = require('grpc');
var protoLoader = require('@grpc/proto-loader');
var packageDefinition = protoLoader.loadSync(
  PROTO_PATH,
  {
    keepCase: true,
    longs: String,
    enums: String,
    defaults: true,
    oneofs: true
  });
var protoDescriptor = grpc.loadPackageDefinition(packageDefinition);

var grpcChat = protoDescriptor.io.mark.grpc.grpcChat;
var client = new grpcChat.ChatService('localhost:50050',
  grpc.credentials.createInsecure());
const readline = require("readline");
const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout
});

var user = process.argv[2];

const regex = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;

if (!regex.test(String(user).toLowerCase())) {
  console.log("usuário inválido! insira outro email")
  process.exit()
}

console.log("Bem vindo, ", user)
var metadata = new grpc.Metadata();
metadata.add('username', user);
var call = client.chat(metadata);

call.on('data', function (ChatMessage) {
  console.log(`${ChatMessage.from} ==> ${ChatMessage.message}`);
});
call.on('end', function () {
  console.log('Servidor encerrado');
});
call.on('error', function (e) {
  console.log(e);
});


rl.on("line", function (line) {
  if (line === "quit") {
    call.end();
    rl.close();
  } else {
    call.write({
      message: line
    });
  }
});



console.log('Insira as suas mensagens:');