// The Grinder
// Copyright (C) 2000, 2001  Paco Gomez
// Copyright (C) 2000, 2001  Philip Aston

// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.

// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

package net.grinder.statistics;

/**
 * Package scope
 *
 * @author Philip Aston
 * @version $Revision$
 */
public class Statistics implements Cloneable, java.io.Serializable
{
    private long m_untimedTransactions = 0;

    private long m_timedTransactions = 0;
    private long m_totalTime = 0;

    private long m_errors = 0;
    private long m_abortions = 0;

    private Statistics m_snapshot = null;

    public Statistics()
    {
    }
    
    private Statistics(long transactions, long timedTransactions,
		       long totalTime, long errors, long abortions)
    {
	m_untimedTransactions = transactions;
	m_timedTransactions = timedTransactions;
	m_totalTime = totalTime;
	m_errors = errors;
	m_abortions = abortions;
    }

    public synchronized void addTransaction()
    {
	m_untimedTransactions++;
    }
	
    public synchronized void addTransaction(long time)
    {
	m_timedTransactions++;
	m_totalTime += time;
    }
	
    public synchronized void addError()
    {
	m_errors++;
    }

    public synchronized void addAbortion()
    {
	m_abortions++;
    }

    /**
     * Protected.
     */
    protected synchronized Statistics getClone()
    {
	try {
	    return (Statistics)clone();
	}
	catch (CloneNotSupportedException e) {
	    throw new Error("0==1,Statistics does not support clone");
	}
    }

    /**
     * Return a Statistics representing the change since the last
     * snapshot. This is the only method that accesses the m_snapshot
     * object so we don't worry about the synchronisation to it.
     */
    public synchronized Statistics getDelta(boolean updateSnapshot)
    {
	final Statistics result;

	if (m_snapshot == null) {
	    result = getClone();
	}
	else {
	    result =
		new Statistics(m_untimedTransactions -
			       m_snapshot.m_untimedTransactions,
			       m_timedTransactions -
			       m_snapshot.m_timedTransactions,
			       m_totalTime - m_snapshot.m_totalTime,
			       m_errors - m_snapshot.m_errors,
			       m_abortions - m_snapshot.m_abortions);
	}

	if (updateSnapshot) {
	    m_snapshot = null;	// Discard history.
	    m_snapshot = getClone();
	}

	return result;
    }

    /**
     * Assumes we don't need to synchronise access to operand.
     */
    public synchronized void add(Statistics operand)
    {
	m_untimedTransactions += operand.m_untimedTransactions;
	m_timedTransactions += operand.m_timedTransactions;
	m_totalTime += operand.m_totalTime;
	m_errors += operand.m_errors;
	m_abortions += operand.m_abortions;
    }

    /** Accessor. N.B. Use clone() to get a consistent snapshot of a
     * changing Statistics */
    public long getTransactions() 
    {
	return m_untimedTransactions + m_timedTransactions;
    }

    /** Accessor. N.B. Use clone() to get a consistent snapshot of a
     * changing Statistics */
    public long getErrors()
    {
	return m_errors;
    }

    /** Accessor. N.B. Use clone() to get a consistent snapshot of a
     * changing Statistics */
    public long getAbortions()
    {
	return m_abortions;
    }

    public synchronized double getAverageTransactionTime()
    {
	if (m_timedTransactions == 0) {
	    return Double.NaN;
	}
	else {
	    return m_totalTime/(double)m_timedTransactions;
	}
    }

    public boolean equals(Object o)
    {
	if (o == this) {
	    return true;
	}
	
	if (!(o instanceof Statistics)) {
	    return false;
	}

	final Statistics theOther = (Statistics)o;

	return
	    m_untimedTransactions == theOther.m_untimedTransactions &&
	    m_timedTransactions == theOther.m_timedTransactions &&
	    m_totalTime == theOther.m_totalTime &&
	    m_errors == theOther.m_errors &&
	    m_abortions == theOther.m_abortions;
    }
}
