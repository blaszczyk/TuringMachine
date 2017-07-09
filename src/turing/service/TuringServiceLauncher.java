package turing.service;

import org.apache.logging.log4j.LogManager;

import bn.blaszczyk.rosecommon.RoseException;
import bn.blaszczyk.rosecommon.tools.CommonPreference;
import bn.blaszczyk.rosecommon.tools.LoggerConfigurator;
import bn.blaszczyk.rosecommon.tools.Preference;
import bn.blaszczyk.rosecommon.tools.Preferences;
import bn.blaszczyk.rosecommon.tools.TypeManager;
import bn.blaszczyk.roseservice.Launcher;
import bn.blaszczyk.roseservice.tools.ServicePreference;

public class TuringServiceLauncher extends Launcher {

	private static final Preference[][] PREFERENCES = new Preference[][]{ServicePreference.values(),CommonPreference.values()};
	
	private static final String ROSE_FILE = "turing/resources/tm.rose";

	@Override
	public Preference[][] getPreferences()
	{
		return PREFERENCES;
	}
	
	public static void main(String[] args)
	{
		Preferences.setMainClass(TuringServiceLauncher.class);
		Preferences.cacheArguments(args, PREFERENCES);
		TypeManager.parseRoseFile(TuringServiceLauncher.class.getClassLoader().getResourceAsStream(ROSE_FILE));
		LoggerConfigurator.configureLogger(CommonPreference.BASE_DIRECTORY, CommonPreference.LOG_LEVEL);
		try
		{
			new TuringServiceLauncher().launch();
		}
		catch (RoseException e)
		{
			LogManager.getLogger(TuringServiceLauncher.class).error("Fehler beim starten des Service", e);
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
