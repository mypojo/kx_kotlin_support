//package net.kotlinx.aws.ses;
//
//
//import com.google.common.base.Preconditions;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import jakarta.activation.DataHandler;
//import jakarta.activation.DataSource;
//import jakarta.activation.FileDataSource;
//import jakarta.mail.*;
//import jakarta.mail.internet.*;
//import jakarta.mail.search.SearchTerm;
//import net.kotlinx.core.time.TimeString;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.UnsupportedEncodingException;
//import java.util.Collection;
//import java.util.List;
//import java.util.Map.Entry;
//import java.util.Properties;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//
///**
// * AWS에서 사용하기위해서 다시 만들었다. ㅠㅠ
// * 싱글톤임.
// * 순수 jakarta로 구성
// * finally의  close 다 생략(소스 분리됨 ㅠㅠ)
// * <p>
// * 인증서 문제 발생하는경우 InstallCert  로 간단 등록할것
// */
//public class EmailTool {
//
//    private EmailSetup emailSetup;
//    private Session session;
//    private File attFileTempDir;
//
//    /**
//     * ses 용인경우  startup 할 필요 없음
//     */
//    public static EmailTool of(EmailSetup emailSetup) {
//        EmailTool vo = new EmailTool();
//        vo.emailSetup = emailSetup;
//        return vo;
//    }
//
//    /**
//     * 이거 호출 후 사용할것
//     */
//    public void startup() {
//
//        Properties props = System.getProperties();
//        props.put("mail.transport.protocol", "smtps");
//        props.put("mail.smtp.port", emailSetup.);
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.starttls.enable", "true");
//        props.put("mail.smtp.starttls.required", "true");
//
//        //읽기
//        props.put("mail.pop3.host", emailSetup.getPop3Host());
//        props.put("mail.pop3.port", emailSetup.getPop3Port());
//        props.put("mail.pop3.starttls.enable", "true");
//
//        session = Session.getInstance(props); //A single default session can be shared by multiple applications on the desktop
//    }
//
//    public List<EmailData> list(SearchTerm finder) throws MessagingException, IOException {
//
//        //create the POP3 store object and connect with the pop server
//        Store store = session.getStore("pop3s");
//        store.connect(emailSetup.getPop3Host(), emailSetup.getSmtpUsername(), emailSetup.getSmtpPassword());
//
//        //create the folder object and open it
//        Folder emailFolder = store.getFolder("INBOX");
//        emailFolder.open(Folder.READ_ONLY);
//
//        POP3Folder uf = (POP3Folder) emailFolder;
//
//        Message[] messages = emailFolder.search(finder);
//        log.debug(" -> search msg size : {}", messages.length);
//
//        List<EmailData> mails = Lists.newArrayList();
//
//        for (Message message : messages) {
//
//            String uid = uf.getUID(message); //일단 최적화 무시
//
//            EmailData emailData = new EmailData();
//            emailData.setUid(uid);
//            emailData.setSentDate(message.getSentDate());
//            emailData.setFrom(Stream.of(message.getFrom()).map(v -> v.toString()).collect(Collectors.toList()));
//            emailData.setSubject(message.getSubject());
//            emailData.setFiles(Maps.newHashMap());
//
//            //콘텐츠의 경우 파일이 같이 있다면 본문 내용도 MimeMultipart 로 들어온다.
//            Object content = message.getContent();
//            if (content instanceof MimeMultipart) {
//                MimeMultipart parts = (MimeMultipart) message.getContent();
//                log.debug("  ==> parts cnt : {}", parts.getCount());
//                for (int h = 0; h < parts.getCount(); h++) {
//                    MimeBodyPart part = (MimeBodyPart) parts.getBodyPart(h);
//                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
//                        TimeString ts = new TimeString();
//                        String fileName = MimeUtility.decodeText(part.getFileName());
//                        File megDir = new File(attFileTempDir, uid);
//                        megDir.mkdirs();
//                        File file = new File(megDir, fileName);
//                        part.saveFile(file);
//                        emailData.getFiles().put(file, file.getAbsolutePath());
//                        log.debug("  ==> 파일 다운로드 : {} ({}) {}", file.getAbsolutePath(), StringFormatUtil.toFileSize(file.length()), ts);
//                    } else {
//                        MimeMultipart textPart = (MimeMultipart) part.getContent();
//                        String text = getTextFromMimeMultipart(textPart);
//                        emailData.setContent(text);
//                    }
//
//                }
//            } else {
//                emailData.setContent(content.toString());
//            }
//            mails.add(emailData);
//
//        }
//
//        //close the store and folder objects
//        emailFolder.close(false);
//        store.close();
//
//        return mails;
//    }
//
//
//    /**
//     * 단건 전송
//     */
//    public void send(EmailData emailDatas) {
//        send(Lists.newArrayList(emailDatas));
//    }
//
//    public void send(Collection<EmailData> emailDatas) {
//
//        try {
//            List<MimeMessage> msgs = Lists.newArrayList();
//            for (EmailData emailData : emailDatas) {
//                MimeMessage msg = emailDateToMessage(emailData);
//                msgs.add(msg);
//            }
//
//            try (Transport transport = session.getTransport()) {
//                transport.connect(emailSetup.getSmtpHost(), emailSetup.getSmtpUsername(), emailSetup.getSmtpPassword());
//                for (MimeMessage msg : msgs) {
//                    transport.sendMessage(msg, msg.getAllRecipients());
//                }
//            }
//        } catch (Exception e) {
//            throw ExceptionUtil.toRuntimeException(e);
//        }
//
//    }
//
//    /**
//     * ses 에서도 사용된다
//     */
//    public MimeMessage emailDateToMessage(EmailData emailData) throws MessagingException, UnsupportedEncodingException {
//        MimeMessage msg = new MimeMessage(session);
//        msg.setFrom(new InternetAddress(emailSetup.getFromEmail(), emailSetup.getFromName(), emailSetup.getEncoding()));
//
//        Preconditions.checkNotNull(emailData.getTo());
//        for (Entry<String, String> e : emailData.getTo().entrySet()) {
//            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(e.getKey(), e.getValue(), emailSetup.getEncoding()));
//        }
//        if (emailData.getCc() != null) {
//            for (Entry<String, String> e : emailData.getCc().entrySet()) {
//                msg.addRecipient(Message.RecipientType.CC, new InternetAddress(e.getKey(), e.getValue(), emailSetup.getEncoding()));
//            }
//        }
//        if (emailData.getBcc() != null) {
//            for (Entry<String, String> e : emailData.getBcc().entrySet()) {
//                msg.addRecipient(Message.RecipientType.BCC, new InternetAddress(e.getKey(), e.getValue(), emailSetup.getEncoding()));
//            }
//        }
//
//        msg.setSubject(emailData.getSubject(), emailSetup.getEncoding());
//
//        Multipart multipart = new MimeMultipart();
//
//        BodyPart contentBody = new MimeBodyPart();
//        String emailContent = emailData.getContent();
//        //수신확인 링크 추가.
////        if(Strings.isNullOrEmpty(emailSetup.getCallbackUrl())) emailContent +=  StringFormatUtil.format(emailSetup.getCallbackUrl(), emailData.getEmailId());
//        contentBody.setContent(emailContent, "text/html; charset=" + emailSetup.getEncoding().toUpperCase());
//        multipart.addBodyPart(contentBody);
//
//        if (emailData.getFiles() != null) {
//            for (Entry<File, String> e : emailData.getFiles().entrySet()) {
//                BodyPart fileBody = new MimeBodyPart();
//                DataSource source = new FileDataSource(e.getKey());
//                fileBody.setDataHandler(new DataHandler(source));
//                fileBody.setFileName(MimeUtility.encodeText(e.getValue())); //hmm
//                multipart.addBodyPart(fileBody);
//            }
//        }
//
//        msg.setContent(multipart);
//        return msg;
//    }
//
//    /**
//     * 주워왔다. 상세 html 파싱 등이 필요하면 수정할것
//     */
//    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
//        String result = "";
//        int count = mimeMultipart.getCount();
//        for (int i = 0; i < count; i++) {
//            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
//            if (bodyPart.getContent() instanceof MimeMultipart) {
//                result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
//            } else {
//                result = result + "\n" + bodyPart.getContent();
//                break; // without break same text appears twice in my tests
//            }
//        }
//        return result;
//    }
//
//    //==================================================== setter ======================================================
//
//
//    public void setAttFileTempDir(File attFileTempDir) {
//        this.attFileTempDir = attFileTempDir;
//    }
//}
