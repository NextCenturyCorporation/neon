package com.ncc.setup.gui
import com.ncc.setup.create.ProjectCreator
import com.ncc.setup.model.Project
import groovy.swing.SwingBuilder

import javax.swing.*
/*
 * ************************************************************************
 * Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 * This software code is the exclusive property of Next Century Corporation and is
 * protected by United States and International laws relating to the protection
 * of intellectual property.  Distribution of this software code by or to an
 * unauthorized party, or removal of any of these notices, is strictly
 * prohibited and punishable by law.
 *
 * UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 * SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 * ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND.  ANY USE BY YOU OF
 * THIS SOFTWARE CODE IS AT YOUR OWN RISK.  ALL WARRANTIES OF ANY KIND, EITHER
 * EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 * DISCLAIMED.
 *
 * PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 * OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 * RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 *
 * 
 * @author tbrooks
 */

class SwingApp {

    private Project projectModel

    SwingApp(){
        projectModel = new Project()
    }

    void createGui(){
        SwingBuilder swingBuilder = new SwingBuilder()

        def groovyButtonGroup = swingBuilder.buttonGroup()
        def javaButtonGroup = swingBuilder.buttonGroup()

        swingBuilder.frame(title: "Project Creator", size: [800, 400],
                defaultCloseOperation: JFrame.EXIT_ON_CLOSE, show: true, pack: true){
            vbox(){
                hbox(){
                    label(text: "Please enter project name.", preferredSize: [260, 25])
                    textField(id: "nameId", preferredSize: [260, 25])
                }
                hbox(){
                    label(text: "Please enter a package name.", preferredSize: [260, 25])
                    textField(id: "packageId", preferredSize: [260, 25], text:"com.ncc")
                }
                hbox(){
                    label(text: "Does the project contain java?", preferredSize: [260, 25])

                    javaButtonGroup.with{
                        add radioButton(text: "Yes", actionPerformed: { projectModel.javaCode = true })
                        add radioButton(text: "No", actionPerformed: { projectModel.javaCode = false }, selected: true)
                    }
                }
                hbox(){
                    label(text: "Does the project contain groovy?", preferredSize: [260, 25])

                    groovyButtonGroup.with{
                        add radioButton(text: "Yes", actionPerformed: { projectModel.groovyCode = true })
                        add radioButton(text: "No", actionPerformed: { projectModel.groovyCode = false }, selected: true)
                    }
                }
                panel(){
                    button(text: "Create Project", actionPerformed: {
                        try{
                            ProjectCreator creator = new ProjectCreator(projectModel)
                            creator.createProject()
                            completeLabel.text = "Project ${projectModel.projectName} created successfully."
                            closeButton.visible = true
                        }catch(Exception e){
                            closeButton.visible = false
                            completeLabel.text = "Error creating project. Did you specify a project name?"
                        }
                    })
                }
                hbox(){
                    completeLabel = label(text: " ")
                    closeButton = button(text: "Close", visible: false, actionPerformed: {
                        System.exit(0);
                    })
                }

                bean(projectModel, projectName: bind { nameId.text })
                bean(projectModel, rootPackageName: bind { packageId.text })
            }
        }
    }

    static void main(String [] args){
        final SwingApp app = new SwingApp()
        app.createGui()
    }
}
