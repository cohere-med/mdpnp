/*******************************************************************************
 * Copyright (c) 2012 MD PnP Program.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package org.mdpnp.guis.swing;

import ice.Numeric;
import ice.SampleArray;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.mdpnp.guis.waveform.WaveformPanel;
import org.mdpnp.guis.waveform.WaveformPanelFactory;
import org.mdpnp.guis.waveform.WaveformUpdateWaveformSource;

import com.rti.dds.subscription.SampleInfo;

@SuppressWarnings("serial")
public class ElectroCardioGramPanel extends DevicePanel {

	private final WaveformPanel[] panel;
	private final Date date = new Date();
	private final JLabel time = new JLabel(" "), heartRate = new JLabel(" "), respiratoryRate = new JLabel(" ");
	private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private final static int[] ECG_WAVEFORMS = new int[] {
	    ice.Physio.MDC_ECG_AMPL_ST_I.value(),
	    ice.Physio.MDC_ECG_AMPL_ST_II.value(),
	    ice.Physio.MDC_ECG_AMPL_ST_III.value(),
	};
	
	private final static String[] ECG_LABELS = new String[] {
	    "ECG LEAD I",
	    "ECG LEAD II", 
	    "ECG LEAD III",
	    "ECG LEAD AVF",
	    "ECG LEAD AVL",
	    "ECG LEAD AVR"
	};
	
	private final Map<Integer, WaveformUpdateWaveformSource> panelMap = new HashMap<Integer, WaveformUpdateWaveformSource>();
	public ElectroCardioGramPanel() {
		super(new BorderLayout());
		add(label("Last Sample: ", time, BorderLayout.WEST), BorderLayout.SOUTH);
		
		JPanel waves = new JPanel(new GridLayout(ECG_WAVEFORMS.length, 1));
		WaveformPanelFactory fact = new WaveformPanelFactory();
		panel = new WaveformPanel[ECG_WAVEFORMS.length];
		for(int i = 0; i < panel.length; i++) {
			waves.add(label(ECG_LABELS[i], (panel[i] = fact.createWaveformPanel()).asComponent())/*, gbc*/);
			WaveformUpdateWaveformSource wuws = new WaveformUpdateWaveformSource();
			panel[i].setSource(wuws);
			panelMap.put(ECG_WAVEFORMS[i], wuws);
			panel[i].start();
		}
		add(waves, BorderLayout.CENTER);
		
		JPanel numerics = new JPanel(new GridLayout(2, 1));
		SpaceFillLabel.attachResizeFontToFill(this, heartRate, respiratoryRate);
		JPanel t;
		numerics.add(t = label("Heart Rate", heartRate));
		t.add(new JLabel("BPM"), BorderLayout.EAST);
		numerics.add(t = label("RespiratoryRate", respiratoryRate));
		t.add(new JLabel("BPM"), BorderLayout.EAST);
		add(numerics, BorderLayout.EAST);
		
		setForeground(Color.green);
		setBackground(Color.black);
		setOpaque(true);
	}
	
	@Override
	public void destroy() {
	    for(WaveformPanel wp : panel) {
	        wp.stop();
	    }
	    super.destroy();
	}
	
	public static boolean supported(Set<Integer> identifiers) {
		for(int w : ECG_WAVEFORMS) {
			if(identifiers.contains(w)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void numeric(Numeric numeric, SampleInfo sampleInfo) {
	    switch(numeric.name) {
	    case ice.Physio._MDC_RESP_RATE:
	        respiratoryRate.setText(Integer.toString((int)numeric.value));
	        break;
//	    case ice.MDC_PULS_OXIM_PULS_RATE.VALUE:
	    case ice.Physio._MDC_PULS_RATE:
	        heartRate.setText(Integer.toString((int)numeric.value));
	        break;
	    }
	}
	@Override
	public void sampleArray(SampleArray sampleArray, SampleInfo sampleInfo) {
	    WaveformUpdateWaveformSource wuws = panelMap.get(sampleArray.name);
        if(null != wuws) {
            wuws.applyUpdate(sampleArray);
        }
        date.setTime(sampleInfo.source_timestamp.sec*1000L + sampleInfo.source_timestamp.nanosec / 1000000L);
        time.setText(dateFormat.format(date));
	}

}
