import java.io.*;
import java.net.ConnectException;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;
import java.util.stream.Collectors;

// Proxy server acts as both client and server. Implement methods of both client and server.
// That's why I create two other classes and built and idea which imposes that proxy server
// consists of two objects, client-like and server-like objects.
class ProxyServer {

    public static final int PROXY_PORT_NUMBER = 8888;

    public static void main(String[] args) throws IOException {


        ServerSocket proxyServerSocket = new ServerSocket(PROXY_PORT_NUMBER);
        while (true) {
//            connectionSocket connects client and server to proxy server
            Socket connectionSocket = proxyServerSocket.accept();
            if (connectionSocket != null) {
                Thread thread = new ProxyThreadSide(connectionSocket);
                thread.start();

            }


        }


    }

}


class ProxyThreadSide extends Thread {
    public static final int SERVER_PORT_NUMBER = 8080;
    String time;
    Socket connectionSocket;

    public ProxyThreadSide(Socket connectionSocket) {
        this.time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        this.connectionSocket = connectionSocket;
    }

    @Override
    public void run() {
        String clientSentence;
        String sizeOfHtml = "";
        int sizeOfHtmlValue;
        String isGet = "";
        System.out.println("Thread "+time+" get the message");
        BufferedReader inFromClient = null;
        DataOutputStream outToClient = null;
        BufferedReader inFromServer = null;
        DataOutputStream outToServer = null;
        String splitSentence = "";
        try {

            inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            outToClient = new DataOutputStream(connectionSocket.getOutputStream());


            clientSentence = inFromClient.readLine();             // For getting after the /
            System.out.println("********************* Client Sentence: " + clientSentence);
            if (clientSentence != null && clientSentence != "" && !(clientSentence.split(" ")[0]).equals("CONNECT")) {
                splitSentence = splitUp(clientSentence);
                isGet = splitSentence.split(" ")[0];
                sizeOfHtml = splitSentence.split("/")[1].split(" ")[0];

            }
            // if the request is cached, send it
            if(isCached(sizeOfHtml) && isGet.equals("GET")){
                sendCached(readContent(sizeOfHtml), outToClient);
                System.out.println("L79: sendCached is run.");
            }
            // if the request is not cached, retrieve the message and cache and send it
            else{
                try {
                    sizeOfHtmlValue = Integer.parseInt(sizeOfHtml);
                } catch (NumberFormatException e) {
                    sizeOfHtmlValue = 0;
                }
                if (isGet.equals("GET")) {
                    if (sizeOfHtmlValue <= 9999) {
                        String contentToCache = "";
                        String stringFromServer = "";

                        Socket toServerSocket = new Socket("localhost", SERVER_PORT_NUMBER);
                        outToServer = new DataOutputStream(toServerSocket.getOutputStream());
                        // forward to server
                        outToServer.writeBytes(splitSentence + "\r\n");

                        System.out.println(clientSentence + " has been sent");

                        inFromServer = new BufferedReader(new InputStreamReader(toServerSocket.getInputStream()));
                        System.out.println("Server has respond");

                        // Read from server
                        stringFromServer = inFromServer.readLine();
                        while(stringFromServer != null){
//                            outToClient.writeBytes(stringFromServer + "\r\n");
                            contentToCache += stringFromServer + "\r\n"; // store information to cache
                            stringFromServer = inFromServer.readLine();
                        }
                        if(contentToCache != "")
                            outToClient.writeBytes(contentToCache);
                        // cache the current reply
                        cacheContent(contentToCache, sizeOfHtml);
                        System.out.println("cacheContent is called");

//                        String stringToClient = inFromServer.readLine();
//                        if(stringToClient == null){
//                            System.out.println("stringToClient is null");
//                        }

                        toServerSocket.close();
                        connectionSocket.close();

                    } else {
                        //"Request-URI Too Long" message with error code 414
                        outToClient.writeBytes("HTTP/1.1 414 Request-URI Too Long\r\n");
                        outToClient.writeBytes("Content-Type: text/html\r\n\r\n");
//                    outToClient.writeBytes("<html>" +
//                            "<head><TITLE>Request-URI Too Long</TITLE></head>" +
//                            "<body><h1>Request-URI Too Long</h1></body>" +
//                            "</html>");
                        System.out.println("414 Request-URI Too Long");
                        connectionSocket.close();

                    }

                }

            }
        } catch (ConnectException connectException){
            try {
//                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                outToClient.writeBytes("HTTP/1.1 404 Not Found\r\n");
                outToClient.writeBytes("Content-Type: text/html\r\n\r\n");
//                outToClient.writeBytes("<html>" +
//                        "<head><TITLE>Not Found</TITLE></head>" +
//                        "<body><h1>404 Not Found</h1></body>" +
//                        "</html>");
                System.out.println("Thread "+time+" has send the message");
                connectionSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } catch ( IOException e) {
            System.out.println("IOexception");
            e.printStackTrace();
        }


    }

    public String splitUp(String str){
        String[] splitSpace = str.split(" ");
        String uri = splitSpace[1].split("/")[3];
        return splitSpace[0] + " /" + uri + " " + splitSpace[2];
    }

    public String readContent(String fileName) throws FileNotFoundException {
        File file = new File("C:\\Users\\omero\\IdeaProjects\\NetworkProject\\cache\\"
                + fileName + ".txt");
        Scanner reader = new Scanner(file);
        String content = "";
        while ( reader.hasNextLine() ){
            content += reader.nextLine() + "\r\n";
        }
        return content;
    }

    public void cacheContent(String content, String fileName){
        File file = new File("C:\\Users\\omero\\IdeaProjects\\NetworkProject\\cache\\"
                + fileName + ".txt");
        try {
            FileWriter writer = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write(content);
            bufferedWriter.close();
            System.out.println("Successfully cached");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isCached(String fileName){
        File file = new File("C:\\Users\\omero\\IdeaProjects\\NetworkProject\\cache\\"
                + fileName + ".txt");
        return file.exists();
    }

    public void sendCached(String content, DataOutputStream outputStream){
        try {
            outputStream.writeBytes(content);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }


}
