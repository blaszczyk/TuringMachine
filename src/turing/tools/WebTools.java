package turing.tools;

import bn.blaszczyk.roseservice.web.HtmlBuilder;
import turing.model.*;

import static bn.blaszczyk.roseservice.web.HtmlTools.*;

import bn.blaszczyk.rosecommon.tools.EntityUtils;

public class WebTools
{

	public static String createOperationPage(final TuringMachine machine) 
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
		builder.append("</table>");
		
		builder.append("<form method=\"post\" action=\"/turing/" + machine.getId() + "/step\">");
		builder.append(input("submit", "step", "step"));
		builder.append("</form>");
		builder.append(linkTo("edit","turing",machine.getId(),"edit"));
		return builder.build();
	}

	public static String createEditPage(final TuringMachine machine)
	{
		final Status status = machine.getStatus();
		final HtmlBuilder builder = new HtmlBuilder();
		builder.h1("Edit Status for " + machine.getName())
			.h2("Tape")
			.append(editTape(status.getCurrentCell(),machine.getId()));
		return builder.build();
	}

	private static String editTape(final TapeCell currentCell, final int id)
	{
		final StringBuilder builder = new StringBuilder();
		builder.append("<form method=\"post\" action=\"/turing/" + id + "/editTape\">");
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
		return builder.toString();
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
