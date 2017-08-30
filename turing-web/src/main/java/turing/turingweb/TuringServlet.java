package turing.turingweb;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinServlet;

import bn.blaszczyk.rose.RoseException;
import bn.blaszczyk.rosecommon.tools.Preferences;
import bn.blaszczyk.rosecommon.tools.TypeManager;
import bn.blaszczyk.roseweb.RoseWebUI;


@WebServlet(urlPatterns = "/*", name = "turingservlet", asyncSupported = true)
@VaadinServletConfiguration(ui = RoseWebUI.class, productionMode = false)
public class TuringServlet extends VaadinServlet {

	private static final long serialVersionUID = -1132289994117995097L;
	
	private static final String ROSE_FILE = "turing/resources/tm.rose";
	
	@Override
	public void init(final ServletConfig servletConfig) throws ServletException
	{
		super.init(servletConfig);
		try
		{
			TypeManager.parseRoseFile(this.getClass().getClassLoader().getResourceAsStream(ROSE_FILE));
			Preferences.setMainClass(TuringServlet.class);
		}
		catch(final RoseException e)
		{
			throw new ServletException("error initializing turing web servlet", e);
		}
	}
    	
}
