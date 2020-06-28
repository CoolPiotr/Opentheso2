package fr.cnrs.opentheso.bean.condidat;

import fr.cnrs.opentheso.bean.condidat.dao.MessageDao;
import fr.cnrs.opentheso.bean.condidat.dto.MessageDto;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.utils.EmailUtils;

import java.io.Serializable;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang3.StringUtils;


@Named(value = "discussionService")
@SessionScoped
public class DiscussionService implements Serializable {

    @Inject
    private CandidatBean candidatBean;

    @Inject
    private Connect connect;

    private String email;

    public List<String> getParticipantsInConversation() {
        if (candidatBean != null && candidatBean.getCandidatSelected() != null) {
            return new MessageDao().getParticipantsByCandidat(
                    connect, candidatBean.getCandidatSelected().getIdConcepte(),
                    candidatBean.getCandidatSelected().getIdThesaurus());
        } else {
            return new ArrayList<>();
        }
    }

    public void sendMessage() throws SQLException {

        MessageDto messageDto = new MessageDto();
        messageDto.setDate(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        messageDto.setNom(candidatBean.getCurrentUser().getUsername().toUpperCase());
        messageDto.setMsg(candidatBean.getMessage());

        new CandidatService().addNewMessage(connect, candidatBean.getMessage(),
                candidatBean.getCurrentUser().getNodeUser().getIdUser() + "",
                candidatBean.getCandidatSelected().getIdConcepte(),
                candidatBean.getCandidatSelected().getIdThesaurus());

        candidatBean.getCandidatSelected().setMessages(new CandidatService()
                .getAllMessagesCandidat(connect,
                        candidatBean.getCandidatSelected().getIdConcepte(),
                        candidatBean.getCandidatSelected().getIdThesaurus(),
                        candidatBean.getCurrentUser().getUsername()));

        candidatBean.setMessage("");

        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Message envoyé !", null);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public void sendInvitation() {

        FacesMessage msg = null;

        if (StringUtils.isEmpty(email)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Vous n'avez pas précisé une adresse mail !", null);
        } else if (!EmailUtils.isValidEmailAddress(email)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "votre adresse email n'est pas valide !", null);
        } else {

            String from = "opentheso@mom.fr";
            String host = "smtp.mom.fr";

            Properties properties = System.getProperties();
            properties.setProperty("mail.smtp.host", host);

            Session session = Session.getDefaultInstance(properties);

            try {
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(from));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
                message.setSubject("Invitation à une conversation !");
                message.setText("C'est le body du message");

                // Send message
                Transport.send(message);

                msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Invitation envoyée !", null);
            } catch (MessagingException mex) {
                mex.printStackTrace();
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Une erreur est survenu pendant l'envoie de l'invitation !", null);
            }
        }

        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
