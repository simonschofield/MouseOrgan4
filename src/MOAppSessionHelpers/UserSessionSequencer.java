package MOAppSessionHelpers;
import java.util.ArrayList;

import MOApplication.Surface;

//////////////////////////////////////////////////////////////////////////////////
//
// a very simple state sequencer. like a finite state machine .
// Allows the user to create named sequences of events.
// During the execution of the sequence it expects to find and call the methods
// InitialiseSequence(String what), UpdateSequence(String what), FinaliseSequence(String what)
// in the UserSessionSlass.(That's why it's a helper!)
public class UserSessionSequencer {
	
	static final int NOTSTARTED = 0;
	static final int INITIALISE = 1;
	static final int UPDATE = 2;
	static final int FINALISE = 3;
	static final int ALLENDED = 4;
	
	
	
	private Surface theUserSession;
	int currentSequenceNumber = 0;
	
	ArrayList<String> theSequence = new ArrayList<String> ();
	
	int currentSequenceState = NOTSTARTED;
	
	public UserSessionSequencer(Surface us) {
		theUserSession = us;
	}
	
	
	public void addSequence(String sq) {
		theSequence.add(sq);
	}
	
	public void addSequences(String[] sqncs) {
		for (String s : sqncs) {
			theSequence.add(s);
		}
		
	}
	
	public boolean update() {
		// always returns true until the final sequence finishes
		if(hasFinished()) return false;
		String currentSequence = getCurrentSequenceName();
		if(currentSequenceState==NOTSTARTED) {
			currentSequenceState = INITIALISE;
			theUserSession.initialiseSequence(currentSequence);
			currentSequenceState = UPDATE;
			return true;
		}
		
		if(currentSequenceState==UPDATE) {
			boolean moreUpdates = theUserSession.updateSequence(currentSequence);
			if(moreUpdates) {
				currentSequenceState = UPDATE;
			} else {
				currentSequenceState = FINALISE;
			}
			return true;
		}
		
		
		
		if(currentSequenceState == FINALISE) {
			theUserSession.finaliseSequence(currentSequence);
			boolean moreSequences = nextSequence();
			if(moreSequences) {
				currentSequenceState = NOTSTARTED;
			} else {
				currentSequenceState = ALLENDED;
			}
		}
		
		if(currentSequenceState == ALLENDED) {
			return false;
		}
		return true;
	}
	
	
	
	public String getCurrentSequenceName() {
		// as currentSequenceNumber cannot be greater than theSequence.size(), the final name is the last in the sequence
		return theSequence.get(currentSequenceNumber);
	}
	
	
	public void reset() {
		currentSequenceNumber = 0;
		currentSequenceState = NOTSTARTED;
	}
	
	public void forceEndCurrentSequence() {
		// so the user can abort a current render halfway through, and invoke the current sequence's theUserSession.finaliseSequence() methid,
		// in order to tidy up/save images etc.
		// The forceEnd starts with clicking "End". This calls surface.endUserSession(), whihc in turn makes the 
		// event loop call surface.finaliseUserSession(). In here, you can detect if the session has been aborted
		// by the boolean surface.userSessionAborted, the user can then call this method from userSession.finaliseUserSession()
		// which will do the tidying up/saving etc,
		theUserSession.finaliseSequence(getCurrentSequenceName());
	}
	
	void jumpTo(String sqnc) {
		// tbd
		
	}
	
	public boolean hasFinished() {
		if(currentSequenceNumber >= theSequence.size()) return true;
		return false;
	}
	
	
	private boolean nextSequence() {

		if(currentSequenceNumber < theSequence.size()) {
			currentSequenceNumber++;
			return true;
		}
		return false;
	}
	
	
	
	
}
