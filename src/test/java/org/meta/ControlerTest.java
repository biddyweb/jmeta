package org.meta;

import java.io.IOException;
import java.net.URISyntaxException;

import org.meta.controler.Controler;

public class ControlerTest {
	public static void main(String[] args) {
		try {
			Controler controler = new Controler();
		} catch (IOException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
