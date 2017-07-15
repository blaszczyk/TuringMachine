package turing.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bn.blaszczyk.rosecommon.RoseException;
import bn.blaszczyk.rosecommon.controller.ModelController;
import bn.blaszczyk.roseservice.server.Endpoint;
import turing.model.TuringMachine;
import turing.model.Value;
import turing.tools.TuringTools;
import turing.tools.WebTools;

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
			final String[] split = path.split("\\/");
			final int id = Integer.parseInt(split[0]);
			final TuringMachine machine = controller.getEntityById(TuringMachine.class, id);
			final String webPage;
			if(split.length == 1)
			{
				webPage = WebTools.createOperationPage(machine);
			}
			else if(split[1].equals("edit"))
			{
				webPage = WebTools.createEditPage(machine);
			}
			else
			{
				return HttpServletResponse.SC_NOT_FOUND;
			}
			response.getWriter().write(webPage);
			return HttpServletResponse.SC_OK;
		}
		catch (final Exception e) 
		{
			throw RoseException.wrap(e, "Error GET@/turing");
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
			else if(split[1].equals("editTape"))
			{
				editTape(machine, request.getParameterMap());
			}
			final String webPage = WebTools.createOperationPage(machine);
			response.getWriter().write(webPage);
			return HttpServletResponse.SC_OK;
		}
		catch (final Exception e) 
		{
			throw RoseException.wrap(e, "Error POST@/turing");
		}
	}

	private void editTape(final TuringMachine machine, final Map<String, String[]> parameterMap) throws RoseException
	{
		final int pos = Integer.parseInt(parameterMap.get("pos")[0]);
		final List<Value> valueList= new ArrayList<>();
		int count = 0;
		String[] values = parameterMap.get("cell0");
		while(values != null && values.length > 0)
		{
			final Value value = Value.valueOf(values[0]);
			valueList.add(value);
			values = parameterMap.get("cell" + ++count);
		}
		TuringTools.editTape(controller, machine.getStatus().getCurrentCell(), valueList, pos);
	}

	@Override
	public int put(final String path, final HttpServletRequest request, final HttpServletResponse response) throws RoseException
	{
		return HttpServletResponse.SC_NO_CONTENT;
	}

	@Override
	public int delete(final String path, final HttpServletRequest request, final HttpServletResponse response) throws RoseException
	{
		return HttpServletResponse.SC_NO_CONTENT;
	}

	@Override
	public Map<String, String> status()
	{
		return Collections.singletonMap("endpoint /turing", "active");
	}

}
