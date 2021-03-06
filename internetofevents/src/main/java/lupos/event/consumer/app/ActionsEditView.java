/**
 * Copyright (c) 2007-2015, Institute of Information Systems (Sven Groppe and contributors of LUPOSDATE), University of Luebeck
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 	- Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * 	  disclaimer.
 * 	- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * 	  following disclaimer in the documentation and/or other materials provided with the distribution.
 * 	- Neither the name of the University of Luebeck nor the names of its contributors may be used to endorse or promote
 * 	  products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package lupos.event.consumer.app;

import java.awt.BorderLayout;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import lupos.event.action.Action;
import lupos.event.action.MessageBoxAction;
import lupos.event.action.PlayWavAction;
import lupos.event.action.SlideOutAction;
import lupos.event.action.XmppMessageAction;


@SuppressWarnings("serial")
public class ActionsEditView  extends JPanel {

	private static final Vector<Class<? extends Action>> availableActionTypes = new Vector<Class<? extends Action>>() {{
			add(MessageBoxAction.class);
			add(PlayWavAction.class);
			add(XmppMessageAction.class);
			add(SlideOutAction.class);
	}};
	
	private JComboBox actionTypesBox;
	
	/**
	 * <p>Constructor for ActionsEditView.</p>
	 */
	public ActionsEditView() {
		super.setLayout(new BorderLayout());
		
		this.actionTypesBox = new JComboBox(availableActionTypes);
		super.add(this.actionTypesBox, BorderLayout.CENTER);
	}
	
	/**
	 * <p>getAction.</p>
	 *
	 * @return a {@link lupos.event.action.Action} object.
	 */
	public Action getAction() {
		@SuppressWarnings("unchecked")
		Class<? extends Action> selectedType = (Class<? extends Action>) this.actionTypesBox.getSelectedItem();
		Action action = null;
		try {
			action = selectedType.newInstance();
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
		return action;
	}
}
