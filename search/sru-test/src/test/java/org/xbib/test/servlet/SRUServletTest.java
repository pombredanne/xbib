package org.xbib.test.servlet;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.xbib.sru.service.SRUServlet;

@RunWith(Arquillian.class)
public class SRUServletTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        WebArchive wa = ShrinkWrap.create(WebArchive.class,"test.war")
                .addClass(SRUServlet.class)
                .addAsWebInfResource("web.xml");
        return wa;
    }

    @Test
    public void testGetText() throws Exception {
        System.err.println("waiting ...");
        Thread.sleep(100000);
        System.err.println("... done");
        URL url = new URL("http://localhost:9090/test/sru/hbz/");
        InputStream is = url.openStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String result = br.readLine();
        System.err.println("result=" + result);
    }

}
