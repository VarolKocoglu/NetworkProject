import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;


class Server {
    public static final int SERVER_PORT_NUMBER = 8080;
    public static void main(String argv[]) throws Exception {


        ServerSocket welcomeSocket = new ServerSocket(SERVER_PORT_NUMBER);

        while (true){
            Socket connectionSocket = welcomeSocket.accept();
            if (connectionSocket != null){
                Thread thread = new ServerThreadSide( connectionSocket);
                thread.start();
            }

        }

    }
}

class ServerThreadSide extends Thread {

    String time;
    Socket connectionSocket;

    public ServerThreadSide(Socket connectionSocket) {
        this.time = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        this.connectionSocket = connectionSocket;
    }

    @Override
    public void run() {
        System.out.println("Thread "+time+" get the message");
        try {

            ///////////////////////


            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

            String clientSentence;
            String[] result;
            String sizeOfHtml = "";
            int sizeOfHtmlValue;
            String isGet = "";
            String addToHTML = "";


            clientSentence = inFromClient.readLine();             // For getting after the /
            if (clientSentence != null && clientSentence != "") {
                result = clientSentence.split("/");
                isGet = result[0].substring(0, result[0].length() - 1);
                sizeOfHtml = result[1].substring(0, result[1].length() - 5);
            }
            try {
                sizeOfHtmlValue = Integer.parseInt(sizeOfHtml);
            } catch (NumberFormatException e) {
                sizeOfHtmlValue = 0;
            }
            if (isGet.equals("GET")) {
                if (20000 > sizeOfHtmlValue && sizeOfHtmlValue >= 100) {
                    for (int x = sizeOfHtmlValue; (x - 100) > 0; x--)  // Hocanın verdiği HTML 96 byte sanki ona göre bir ayar çekebilirsin
                    {
                        addToHTML = addToHTML + "b";

                    }
                    Date date = new Date();
                    outToClient.writeBytes("HTTP/1.1 200 OK\r\n");
                    outToClient.writeBytes("Date:" + date + "\r\n");
                    outToClient.writeBytes("Server: " + InetAddress.getLocalHost().getHostName()+"\r\n");
                    outToClient.writeBytes("Last-Modified: " + date+"\r\n");
                    outToClient.writeBytes("Content-Type: text/html\r\n");
                    outToClient.writeBytes("Content-Length: "+ sizeOfHtmlValue +"\r\n\r\n");
                    outToClient.writeBytes("<HTML>\n" +
                            "<HEAD>\n" +
                            "<TITLE>I am 100 bytes long</TITLE>" +
                            "</HEAD>\n" +
                            "<BODY> a a a a a a a a " + addToHTML + "</BODY>\n" +
                            "</HTML>");
                    //          outToClient.flush();



                } else {
                    //"Bad Request" message with error code 400
                    outToClient.writeBytes("HTTP/1.1 400 Bad Request\r\n");
                    outToClient.writeBytes("Content-Type: text/html\r\n\r\n");
//                    outToClient.writeBytes("<html>" +
//                            "<head><TITLE>I am 100 bytes long</TITLE></head>" +
//                            "<body><h1>" + addToHTML + "</h1></body>" +
//                            "</html>");
               //     outToClient.flush();

                }
                System.out.println("Message has send");

            } else{
                //“Not Implemented” (501)
                try {
                    outToClient.writeBytes("HTTP/1.1 501 Not Implemented\r\n");
                    outToClient.writeBytes("Content-Type: text/html\r\n\r\n");
//                outToClient.writeBytes("<html>" +
//                        "<head><TITLE>Not Implemented</TITLE></head>" +
//                        "<body><h1>The method is not Get</h1></body>" +
//                        "</html>");
                    //       outToClient.flush();

                    System.out.println("Thread "+time+" has send the message");
                }
                catch (SocketException se){
                    System.out.println(" ");
                }

            }

            connectionSocket.close();

        } catch ( IOException e) {
            e.printStackTrace();

        }

    }

}
