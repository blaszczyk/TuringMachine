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
import turing.model.Direction;
import turing.model.Program;
import turing.model.State;
import turing.model.Status;
import turing.model.Step;
import turing.model.TapeCell;
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
			final int machineId = Integer.parseInt(split[0]);
			final String webPage;
			if(split.length == 1)
			{
				final TuringMachine machine = controller.getEntityById(TuringMachine.class, machineId);
				webPage = WebTools.createOperationPage(machine, controller.getEntities(State.class), controller.getEntities(Program.class));
			}
			else if(split[1].equals("editTape"))
			{
				if(split.length < 3)
					return HttpServletResponse.SC_NOT_FOUND;
				final int id = Integer.parseInt(split[2]);
				final TapeCell currentCell = controller.getEntityById(TapeCell.class, id);
				webPage = WebTools.createEditTapePage(currentCell, machineId);
			}
			else if(split[1].equals("editState"))
			{
				final int id = Integer.parseInt(request.getParameter("state"));
				final State state = controller.getEntityById(State.class, id);
				webPage = WebTools.createEditStatePage(state, controller.getEntities(State.class),machineId);
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
			final int machineId = Integer.parseInt(split[0]);
			if(split.length < 2)
				return HttpServletResponse.SC_NOT_FOUND;
			final TuringMachine machine = controller.getEntityById(TuringMachine.class, machineId);
			final String webPage;
			switch(split[1])
			{
			case "step":
				final int steps = Integer.parseInt(request.getParameter("stepCount"));
				for(int i = 0; i < steps; i++)
					TuringTools.step(controller,machine);
				webPage = WebTools.createOperationPage(machine, controller.getEntities(State.class), controller.getEntities(Program.class));
				break;
			case "editTape":
				if(split.length < 3)
					return HttpServletResponse.SC_NOT_FOUND;
				final int tapeId = Integer.parseInt(split[2]);
				final TapeCell currentCell = controller.getEntityById(TapeCell.class, tapeId);
				editTape(currentCell, request.getParameterMap());
				webPage = WebTools.createOperationPage(machine, controller.getEntities(State.class), controller.getEntities(Program.class));
				break;
			case "editState":
				if(split.length < 3)
					return HttpServletResponse.SC_NOT_FOUND;
				final int stateId = Integer.parseInt(split[2]);
				final State state = controller.getEntityById(State.class, stateId);
				editState(state, request.getParameterMap());
				webPage = WebTools.createOperationPage(machine, controller.getEntities(State.class), controller.getEntities(Program.class));
				break;
			case "createState":
				final State newState = controller.createNew(State.class);
				webPage = WebTools.createEditStatePage(newState, controller.getEntities(State.class), machineId);
				break;
			case "load":
				final int programId = Integer.parseInt(request.getParameter("program"));
				final Program program = controller.getEntityById(Program.class, programId);
				machine.setProgram(program);
				program.getTuringMachines().add(machine);
				final Status status = machine.getStatus();
				status.setCurrentState(program.getStart());
				program.getStart().getStatuss().add(status);
				status.setRunning(true);
				controller.update(machine,program,status);
				webPage = WebTools.createOperationPage(machine, controller.getEntities(State.class), controller.getEntities(Program.class));
				break;
			default:
				return HttpServletResponse.SC_NOT_FOUND;
			}
			response.getWriter().write(webPage);
			return HttpServletResponse.SC_OK;
		}
		catch (final Exception e) 
		{
			throw RoseException.wrap(e, "Error POST@/turing");
		}
	}

	private void editState(final State state, final Map<String, String[]> parameterMap) throws RoseException
	{	
		final String name = parameterMap.get("name")[0];
		state.setName(name);
		for(final Value value : Value.values())
		{
			final String[] terminatesValues = parameterMap.get(value.name() + "_terminates");
			final boolean terminates = terminatesValues != null && terminatesValues.length > 0;
			final Step step = TuringTools.stepForValue(state, value);
			if(terminates)
			{
				if(step != null)
					controller.delete(step);
			}
			else
			{
				final Value writeValue = Value.valueOf(parameterMap.get(value.name() + "_writeValue")[0]);
				final Direction direction = Direction.valueOf(parameterMap.get(value.name() + "_direction")[0]);
				final int nextStateId = Integer.parseInt(parameterMap.get(value.name() + "_nextState")[0]);
				final State nextState = controller.getEntityById(State.class, nextStateId);
				final Step writeStep = step != null ? step : controller.createNew(Step.class);
				writeStep.setReadValue(value);
				writeStep.setWriteValue(writeValue);
				writeStep.setDirection(direction);
				writeStep.setStateFrom(state);
				state.getStepTos().add(writeStep);
				writeStep.setStateTo(nextState);
				nextState.getStepFroms().add(writeStep);
				controller.update(writeStep);
			}
		}
		controller.update(state);
	}

	private void editTape(final TapeCell currentCell, final Map<String, String[]> parameterMap) throws RoseException
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
		TuringTools.editTape(controller, currentCell, valueList, pos);
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
