package com.getsteam.Controller;

import com.getsteam.Utils.MailUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.annotation.Resource;
import javax.mail.internet.MimeUtility;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
@Controller
@EnableScheduling
public class mailSend {
        @Resource
        private JavaMailSenderImpl mailSender;
        @Autowired
        private MailUtils mailUtils;
        @Value("${spring.mail.username}")
        private String mailto;
        String newmailContent="";
        int runTimes=0;
        @Scheduled(cron = "*/10 * * * * ?")
        public void steammail() throws Exception {
                String USER="发件邮箱账号";
                String PASSWORD = "发件邮箱的临时令牌（可用于登录的令牌，不要泄露）";
                String HOST = "smtp.qq.com";
                Properties prop=new Properties();
                prop.setProperty("mail.store.protocol","pop3");
                prop.setProperty("mail.pop3.host", HOST);
                // 1、创建session
                Session session = Session.getInstance(prop);
                // 2、通过session得到Store对象
                Store store = session.getStore();
                // 3、连上邮件服务器
                store.connect(HOST, USER, PASSWORD);
                // 4、获得邮箱内的邮件夹
                Folder folder = store.getFolder("INBOX");
                //只读
//        folder.open(Folder.READ_ONLY);
                //读写
                folder.open(Folder.READ_WRITE);
                // 获得邮件夹Folder内的所有邮件Message对象
                Message[] messages = folder.getMessages();
                // 解析所有邮件
//                System.out.println("邮件数:"+messages.length);
                Folder folders[ ]=store.getDefaultFolder().list();
                for (int i = messages.length-1, count = messages.length; i < count; i++) {
                        MimeMessage msg = (MimeMessage) messages[i];
//                        System.out.println("------------------解析第" + msg.getMessageNumber() + "封邮件-------------------- ");
//                        System.out.println("主题: " + getSubject(msg));
//                        System.out.println("发件人: " + getFrom(msg));
//                        System.out.println("发送时间：" + getSentDate(msg, null));
                        StringBuffer content = new StringBuffer(30);
                        getMailTextContent(msg, content);
                        if (getFrom(msg).equals("Steam 客服 <noreply@steampowered.com>")||getFrom(msg).equals("Steam Support <noreply@steampowered.com>")||getFrom(msg).equals("ฝ่ายสนับสนุน Steam <noreply@steampowered.com>")){
                                System.out.println("steam令牌接收，检测是否最新");
                                String cont=new String(content);

                         if(!cont.equals(newmailContent)){
                               newmailContent=cont;
                               mailUtils.sendSimpleEmail("收件人邮箱地址", "steam令牌邮件转发", content.substring(0,200));
                               mailUtils.sendSimpleEmail("收件人邮箱地址，这里填写自己可以方便查看邮件是否成功转发","单次steam临牌已发送",content.substring(0,200));
//                                 System.out.println("cont:"+cont.substring(0,50));
//                                 System.out.println("newmail:"+newmailContent.substring(0,50));
                        }
                        }
                }
                runTimes++;
                System.out.println("--------------------------------单次进程结束"+runTimes+"---------------------------------");
                // 5、关闭
                folder.close(true);
                store.close();
        }
        public String getSentDate(MimeMessage msg, String pattern) throws MessagingException {
                Date receivedDate = msg.getSentDate();
                if (receivedDate == null)
                        return "";
                if (pattern == null || "".equals(pattern))
                        pattern = "yyyy年MM月dd日 E HH:mm ";
                return new SimpleDateFormat(pattern).format(receivedDate);
        }
        public String getSubject(MimeMessage msg) throws UnsupportedEncodingException, MessagingException {
                return MimeUtility.decodeText(msg.getSubject());
        }
        public void getMailTextContent(Part part, StringBuffer content) throws MessagingException, IOException {
                //如果是文本类型的附件，通过getContent方法可以取到文本内容，但这不是我们需要的结果，所以在这里要做判断
                boolean isContainTextAttach = part.getContentType().indexOf("name") > 0;
                if (part.isMimeType("text/*") && !isContainTextAttach) {
                        content.append(part.getContent().toString());
                } else if (part.isMimeType("message/rfc822")) {
                        getMailTextContent((Part) part.getContent(), content);
                } else if (part.isMimeType("multipart/*")) {
                        Multipart multipart = (Multipart) part.getContent();
                        int partCount = multipart.getCount();
                        for (int i = 0; i < partCount; i++) {
                                BodyPart bodyPart = multipart.getBodyPart(i);
                                getMailTextContent(bodyPart, content);
                        }
                }
        }
        public String getFrom(MimeMessage msg) throws MessagingException, UnsupportedEncodingException {
                String from = "";
                Address[] froms = msg.getFrom();
                if (froms.length < 1)
                        throw new MessagingException("没有发件人!");
                InternetAddress address = (InternetAddress) froms[0];
                String person = address.getPersonal();
                if (person != null) {
                        person = MimeUtility.decodeText(person) + " ";
                } else {
                        person = "";
                }
                from = person + "<" + address.getAddress() + ">";

                return from;
        }

//        测试类
        @ResponseBody
        @RequestMapping("/Mail")
        public void sendEmail() {
            mailUtils.sendSimpleEmail("本接口测试专用", "123", "456");
        }
}
