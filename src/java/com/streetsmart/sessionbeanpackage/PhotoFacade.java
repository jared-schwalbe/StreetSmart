/*
 * Created by Mykhaylo Bulgakov, Mukund Katti, Jared Schwalbe, Tim Street, and Hung Vu on 2016.04.19  * 
 * Copyright © 2016 Mykhaylo Bulgakov, Mukund Katti, Jared Schwalbe, Tim Street, and Hung Vu. All rights reserved. * 
 */
package com.streetsmart.sessionbeanpackage;

import com.streetsmart.entitypackage.Photo;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Kevin
 */
@Stateless
public class PhotoFacade extends AbstractFacade<Photo> {

    @PersistenceContext(unitName = "StreetSmartPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public PhotoFacade() {
        super(Photo.class);
    }
    
    // The following findPhotosByUserID method is added to the generated code.
    
    /**
     * Finds the photos attached to the UserID.
     * @param userID
     * @return a list of the photos
     */
    public List<Photo> findPhotosByUserID(Integer userID) {
        return (List<Photo>) em.createNamedQuery("Photo.findPhotosByUserId")
                .setParameter("userId", userID)
                .getResultList();
    }
    
}
