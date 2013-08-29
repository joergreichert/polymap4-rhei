/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.rhei.um.email;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EmailService {

    private static Log log = LogFactory.getLog( EmailService.class );
    
    
    public static EmailService instance() {
        throw new RuntimeException( "not yet implemented." );
    }

    
    // instance ******************************************* 
    
    public void send( String templateName, Map<String,String> replacements ) {
//        Session session = Session.getDefaultInstance(properties);
//
//        try{
//           // Create a default MimeMessage object.
//           MimeMessage message = new MimeMessage(session);
//
//           // Set From: header field of the header.
//           message.setFrom(new InternetAddress(from));
//
//           // Set To: header field of the header.
//           message.addRecipient(Message.RecipientType.TO,
//                                    new InternetAddress(to));
//
//           // Set Subject: header field
//           message.setSubject("This is the Subject Line!");
//
//           // Now set the actual message
//           message.setText("This is actual message");
//
//           // Send message
//           Transport.send(message);
//           System.out.println("Sent message successfully....");
//        }catch (MessagingException mex) {
//           mex.printStackTrace();
//        }
    }
}
