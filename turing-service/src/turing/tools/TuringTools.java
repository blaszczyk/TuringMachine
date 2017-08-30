package turing.tools;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bn.blaszczyk.rose.RoseException;
import bn.blaszczyk.rosecommon.controller.ModelController;
import bn.blaszczyk.rosecommon.tools.EntityUtils;
import turing.model.Direction;
import turing.model.Program;
import turing.model.State;
import turing.model.Status;
import turing.model.Step;
import turing.model.TapeCell;
import turing.model.TuringMachine;
import turing.model.Value;

public class TuringTools
{
	private static final Logger LOGGER = LogManager.getLogger(TuringTools.class);
	
	static final int SHOW_CELLS_RADIUS = 5;

	public static void step(final ModelController controller, final TuringMachine machine) throws RoseException
	{
		final Status status = machine.getStatus();
		if(!status.isRunning())
			return;
		final State state = status.getCurrentState();
		final TapeCell cell = status.getCurrentCell();
		final Value value = cell.getValue();
		for(final Step step : state.getStepTos())
			if(step.getReadValue().equals(value))
			{
				final Value writeValue = step.getWriteValue();
				cell.setValue(writeValue);
				
				final boolean directionRight = step.getDirection().equals(Direction.RIGHT);
				TapeCell nextCell = directionRight ? cell.getNext() : cell.getPrevious();
				if(nextCell == null)
				{
					nextCell = controller.createNew(TapeCell.class);
					cell.setEntity(directionRight ? TapeCell.NEXT : TapeCell.PREVIOUS, nextCell);
				}
				status.setEntity(Status.CURRENTCELL, nextCell);

				final State nextState = step.getStateTo();
				if(!EntityUtils.equals(state, nextState))
				{
					status.setEntity(Status.CURRENTSTATE, nextState);
					controller.update(state, nextState);
				}
				
				controller.update(status, cell, nextCell);
				
				LOGGER.info(String.format("step: machine %s with program %s", machine.getName(), machine.getProgram().getName()));
				LOGGER.info(String.format("step: from state %s to state %s.", state.getName(), nextState.getName()));
				LOGGER.info("step: tape direction = " + step.getDirection());
				LOGGER.info("Tape: " + tapeToString(SHOW_CELLS_RADIUS, status.getCurrentCell(), "[%s]"));
				return;
			}
		status.setRunning(false);
		controller.update(status);
		LOGGER.info(String.format("terminating %s with program %s at state %s.", machine.getName(), machine.getProgram().getName(), state.getName()));
		LOGGER.info("Tape: " + tapeToString(SHOW_CELLS_RADIUS, status.getCurrentCell(), "[%s]"));
	}
	
	static String tapeToString(final int radius, final TapeCell currentCell, final String highlighter)
	{
		TapeCell showCell = currentCell;
		final StringBuilder builder = new StringBuilder();
		for(int i = 0; i < radius; i++)
		{
			if(showCell.getPrevious() == null)
				break;
			showCell = showCell.getPrevious();
		}
		if(showCell.getPrevious() != null)
			builder.append("... - ");
		while(showCell != currentCell)
		{
			builder.append(showCell.getValue() + " - " );
			showCell = showCell.getNext();
		}
		builder.append(String.format(highlighter, showCell.getValue()));
		for(int i = 0; i < radius; i++)
		{
			showCell = showCell.getNext();
			if(showCell == null)
				break;
			builder.append( " - " + showCell.getValue());
		}
		if(showCell != null && showCell.getNext() != null)
			builder.append(" - ...");
		return builder.toString();
	}

	public static void editTape(final ModelController controller, final TapeCell currentCell, final List<Value> values, final int pos) throws RoseException
	{
		TapeCell cell = isCyclic(currentCell) ? currentCell : getFirst(currentCell);
		int countDown = pos;
		for(final Value value : values)
		{
			cell.setValue(value);
			if(countDown == 0)
			{
				final Status status = currentCell.getStatus();
				cell.setEntity(TapeCell.STATUS, status);
			}
			controller.update(cell);
			countDown--;
			cell = cell.getNext();
		}
	}
	
	public static void loadProgram(final int programId, final TuringMachine machine, final ModelController controller) throws RoseException
	{
		final Program program = controller.getEntityById(Program.class, programId);
		machine.setEntity(TuringMachine.PROGRAM, program);
		final Status status = machine.getStatus();
		status.setEntity(Status.CURRENTSTATE, program.getStart());
		status.setRunning(true);
		controller.update(machine,program,status);
	}
	
	public static boolean isCyclic(final TapeCell currentCell)
	{
		TapeCell cell = currentCell.getPrevious();
		while(true)
		{
			if(cell == null)
				return false;
			if(cell == currentCell)
				return true;
			cell = cell.getPrevious();
		}
	}
	
	public static TapeCell getFirst(final TapeCell currentCell)
	{
		TapeCell cell = currentCell;
		while(cell.getPrevious() != null)
			cell = cell.getPrevious();
		return cell;
	}

	public static Step stepForValue(final State state, final Value value)
	{
		for(final Step step : state.getStepTos())
			if(step.getReadValue().equals(value))
				return step;
		return null;
	}
	
	public static Status statusForCell(final TapeCell anyCell)
	{
		TapeCell cell = isCyclic(anyCell) ? anyCell : getFirst(anyCell);
		while(cell.getStatus() == null)
		{
			if(cell.getNext() == null || cell.getNext() == anyCell)
				return null;
			cell = cell.getNext();
		}
		return cell.getStatus();
	}

}
