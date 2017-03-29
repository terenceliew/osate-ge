package org.osate.ge.internal.query;

import java.util.Deque;
import java.util.Objects;
import java.util.function.Supplier;

public class RootQuery extends Query<Object> {
	private final Supplier<Queryable> supplier;
	
	public RootQuery(final Supplier<Queryable> supplier) {
		super(null);
		this.supplier = Objects.requireNonNull(supplier, "supplier must not be null");
	}

	@Override
	void run(final Deque<Query<Object>> remainingQueries, final Queryable ctx, final QueryExecutionState<Object> state, final QueryResult result) {		
		final Queryable suppliedObject = supplier.get();
		if(suppliedObject == null) {
			result.done = true;
			return;
		}

		processResultValue(remainingQueries, suppliedObject, state, result);
	}
}
