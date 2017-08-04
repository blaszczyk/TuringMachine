package turing.tools;

import bn.blaszczyk.roseservice.web.HtmlBuilder;
import turing.model.*;

import static bn.blaszczyk.roseservice.web.HtmlTools.*;

import java.util.List;

import bn.blaszczyk.rosecommon.tools.EntityUtils;

public class WebTools
{

	public static String createOperationPage(final TuringMachine machine, final List<State> allStates, final List<Program> allPrograms) 
	{
		final HtmlBuilder builder = new HtmlBuilder();
		
		final Status status = machine.getStatus();
		builder.h1("Turing Machine: " + machine.getName());
		builder.h2("Program: " + machine.getProgram().getName());
		builder.append("running: " + status.isRunning());
		
		builder.h2("Tape:");
		final TapeCell currentCell = status.getCurrentCell();
		builder.append(TuringTools.tapeToString(TuringTools.SHOW_CELLS_RADIUS, currentCell, "<b>%s</b>"));

		
		final State state = status.getCurrentState();
		builder.h2("Current State: " + state.getName());
		builder.append("Possible next states:<br>");
		builder.append("<table>");
		builder.append("<tr><th>read value</th><th>write value</th><th>tape direction</th><th>state name</th></tr>");
		for(final Step step : state.getStepTos())
			builder.append(String.format("<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>",step.getReadValue(), step.getWriteValue(), step.getDirection(), step.getStateTo().getName()));
		builder.append("</table><br><br>");
		
		builder.append("<form method=\"post\" action=\"/turing/" + machine.getId() + "/step\">");
		builder.append(input("text", "stepCount", 1));
		builder.append(input("submit", "steps", "steps"));
		builder.append("</form>");
		builder.append("<br><form method=\"get\" action=\"/turing/")
			.append(machine.getId())
			.append("/editTape/")
			.append(machine.getStatus().getCurrentCell().getId())
			.append("\">")
			.append(input("submit", "editTape", "edit Tape"))
			.append("</form>");
		builder.append("<br><form method=\"post\" action=\"/turing/")
		.append(machine.getId())
		.append("/load\">")
		.append(input("submit", "load", "load Program"))
		.append(selectProgram("program", machine.getProgram(), allPrograms))
		.append("</form>");
		builder.append("<br><form method=\"get\" action=\"/turing/")
		.append(machine.getId())
		.append("/editState\">")
		.append(input("submit", "editState", "edit State"))
		.append(selectState("state", state, allStates))
		.append("</form>");
		builder.append("<br><form method=\"post\" action=\"/turing/")
		.append(machine.getId())
		.append("/createState\">")
		.append(input("submit", "createState", "create State"))
		.append("</form>");
		return builder.build();
	}

	public static String createEditTapePage(final TapeCell currentCell, final int machineId)
	{
		final HtmlBuilder builder = new HtmlBuilder();
		builder.h1("Edit Tape");
		builder.append("<form method=\"post\" action=\"/turing/" + machineId + "/editTape/" + currentCell.getId() + "\">");
		final boolean cyclic = TuringTools.isCyclic(currentCell);
		TapeCell cell = cyclic ? currentCell : TuringTools.getFirst(currentCell);
		int count = 0;
		int currentPos = -1;
		while(cell != null)
		{
			builder.append(selectValue("cell" + count, cell.getValue()))
			.append(" pos = ")
			.append(count++)
			.append("<br>");
			if(EntityUtils.equals(cell, currentCell))
			{
				currentPos = count;
				if(cyclic)
					break;
			}
			cell = cell.getNext();
		}
		builder.append("<br>");
		if(cyclic)
			builder.append("cyclic<br>");
		builder.append("<br>current cell:");
		builder.append(input("text", "pos", cyclic ? 0 : --currentPos));
		builder.append("<br>");
		builder.append(input("submit", "save", "save"));
		builder.append("</form>");
		return builder.build();
	}

	public static String createEditStatePage(final State state, final List<State> allStates, final int machineId)
	{
		final HtmlBuilder builder = new HtmlBuilder();
		builder.h1("Edit State: " + state.getName() + " (id=" + state.getId() + ")")
			.append("<form method = \"post\" action=\"/turing/" + machineId + "/editState/" + state.getId() + "   \">")
			.append(input("text", "name", state.getName()))
			.append("<table><tr><th>read Value</th><th>terminate</th><th>write Value</th><th>tape Direction</th><th>next State</th></tr>");
		for(final Value value : Value.values())
		{
			final Step step = TuringTools.stepForValue(state, value);
			builder.append("<tr><td><b>")
				.append(value.name())
				.append("</b></td><td>")
				.append(input("checkbox", value + "_terminates", "terminates", step == null ? "checked" : ""))
				.append("</td><td>")
				.append(selectValue(value + "_writeValue", step == null ? null : step.getWriteValue()))
				.append("</td><td>")
				.append(selectDirection(value + "_direction", step == null ? null : step.getDirection()))
				.append("</td><td>")
				.append(selectState(value + "_nextState", step == null ? null : step.getStateTo(), allStates))
				.append("</td></tr>");
		}
		builder.append("</table>")
		.append(input("submit", "save", "save"));
		return builder.build();
	}

	private static String selectState(final String name, final State selectedState, final List<State> allStates)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("<select name=\"")
		.append(name)
		.append("\">");
		for(final State state : allStates)
			sb.append("<option value=\"")
			.append(state.getId())
			.append("\"")
			.append(state.equals(selectedState) ? " selected " : "")
		    .append(">")
			.append(state.getName())
			.append("</option>");
		sb.append("</select>");
		return sb.toString();
	}
	

	private static String selectProgram(final String name, final Program selectedProgram, final List<Program> allPrograms)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("<select name=\"")
		.append(name)
		.append("\">");
		for(final Program program : allPrograms)
			sb.append("<option value=\"")
			.append(program.getId())
			.append("\"")
			.append(program.equals(selectedProgram) ? " selected " : "")
		    .append(">")
			.append(program.getName())
			.append("</option>");
		sb.append("</select>");
		return sb.toString();
	}

	private static String selectDirection(final String name, final Direction selectedDirection)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("<select name=\"")
		.append(name)
		.append("\">");
		for(final Direction value : Direction.values())
			sb.append("<option value=\"")
			.append(value.name())
			.append("\"")
			.append(value.equals(selectedDirection) ? " selected " : "")
		    .append(">")
			.append(value.name())
			.append("</option>");
		sb.append("</select>");
		return sb.toString();
	}

	private static String selectValue(final String name, final Value selectedValue)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("<select name=\"")
		.append(name)
		.append("\">");
		for(final Value value : Value.values())
			sb.append("<option value=\"")
			.append(value.name())
			.append("\"")
			.append(value.equals(selectedValue) ? " selected " : "")
		    .append(">")
			.append(value.name())
			.append("</option>");
		sb.append("</select>");
		return sb.toString();
	}

}
