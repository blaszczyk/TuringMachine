package turing.service;

import org.apache.logging.log4j.LogManager;

import bn.blaszczyk.rose.RoseException;
import bn.blaszczyk.rosecommon.tools.CommonPreference;
import bn.blaszczyk.rosecommon.tools.LoggerConfigurator;
import bn.blaszczyk.rosecommon.tools.Preferences;
import bn.blaszczyk.rosecommon.tools.TypeManager;
import bn.blaszczyk.roseservice.Launcher;

public class TuringServiceLauncher extends Launcher {
	
	private static final String ROSE_FILE = "turing/resources/tm.rose";
	
	public static void main(String[] args)
	{
		try
		{
			Preferences.setMainClass(TuringServiceLauncher.class);
			TypeManager.parseRoseFile(ROSE_FILE);
			LoggerConfigurator.configureLogger(CommonPreference.BASE_DIRECTORY, CommonPreference.LOG_LEVEL);
			new TuringServiceLauncher().launch();
		}
		catch (RoseException e)
		{
			LogManager.getLogger(TuringServiceLauncher.class).error("Error launching turing service", e);
		}
	}
	
	
	@Override
	protected void registerEndpoints()
	{
		super.registerEndpoints();
		final TuringEndpoint endpoint = new TuringEndpoint(getController());
		getServer().getHandler().registerEndpoint("turing", endpoint);
	}

}
