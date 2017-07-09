package turing.service;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bn.blaszczyk.rosecommon.RoseException;
import bn.blaszczyk.rosecommon.controller.ModelController;
import bn.blaszczyk.roseservice.server.Endpoint;
import turing.model.TuringMachine;
import turing.tools.TuringTools;

public class TuringEndpoint implements Endpoint
{
	
	private final ModelController controller;
	
	public TuringEndpoint(final ModelController controller)
	{
		this.controller = controller;
	}

	@Override
	public int get(final String path, final HttpServletRequest request, final HttpServletResponse response) throws RoseException
	{
		try
		{
			final int id = Integer.parseInt(path);
			final TuringMachine machine = controller.getEntityById(TuringMachine.class, id);
			final String webPage = TuringTools.createWebPage(machine);
			response.getWriter().write(webPage);
			return HttpServletResponse.SC_OK;
		}
		catch (final Exception e) 
		{
			throw RoseException.wrap(e, "Error GET@/file");
		}
	}

	@Override
	public int post(final String path, final HttpServletRequest request, final HttpServletResponse response) throws RoseException
	{
		try
		{
			final String[] split = path.split("\\/");
			final int id = Integer.parseInt(split[0]);
			final TuringMachine machine = controller.getEntityById(TuringMachine.class, id);
			if(split[1].equals("step"))
			{
				int steps = split.length > 2 ? Integer.parseInt(split[2]) : 1;
				for(int i = 0; i < steps; i++)
					TuringTools.step(controller,machine);
			}
			final String webPage = TuringTools.createWebPage(machine);
			response.getWriter().write(webPage);
			return HttpServletResponse.SC_OK;
		}
		catch (final Exception e) 
		{
			throw RoseException.wrap(e, "Error POST@/file");
		}
	}

	@Override
	public int put(final String path, final HttpServletRequest request, final HttpServletResponse response) throws RoseException
	{
		try
		{
		}
		catch (final Exception e) 
		{
			throw RoseException.wrap(e, "Error PUT@/file");
		}
		return HttpServletResponse.SC_NO_CONTENT;
	}

	@Override
	public int delete(final String path, final HttpServletRequest request, final HttpServletResponse response) throws RoseException
	{
		try
		{
		}
		catch (final Exception e) 
		{
			throw RoseException.wrap(e, "Error DELETE@/file");
		}
		return HttpServletResponse.SC_NO_CONTENT;
	}

	@Override
	public Map<String, String> status()
	{
		return Collections.singletonMap("endpoint /turing", "active");
	}

}
