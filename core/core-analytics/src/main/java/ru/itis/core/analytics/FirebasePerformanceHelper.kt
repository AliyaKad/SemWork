package ru.itis.core.analytics

import com.google.firebase.Firebase
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace as FirebaseTrace
import com.google.firebase.perf.performance
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebasePerformanceHelper @Inject constructor() : PerformanceHelper {

    private val firebasePerformance: FirebasePerformance by lazy {
        Firebase.performance
    }

    override fun createTrace(traceName: String): Trace {
        return FirebaseTraceWrapper(firebasePerformance.newTrace(traceName))
    }

    override fun startTrace(traceName: String): Trace {
        val trace = firebasePerformance.newTrace(traceName)
        trace.start()
        return FirebaseTraceWrapper(trace)
    }

    override fun stopTrace(trace: Trace) {
        (trace as FirebaseTraceWrapper).firebaseTrace.stop()
    }

    override fun measureOperation(traceName: String, block: () -> Unit) {
        val trace = firebasePerformance.newTrace(traceName)
        trace.start()
        try {
            block()
        } finally {
            trace.stop()
        }
    }

    private class FirebaseTraceWrapper(
        val firebaseTrace: FirebaseTrace
    ) : Trace {

        override fun start() {
            firebaseTrace.start()
        }

        override fun stop() {
            firebaseTrace.stop()
        }

        override fun incrementMetric(metricName: String, incrementBy: Long) {
            firebaseTrace.incrementMetric(metricName, incrementBy)
        }
    }
}