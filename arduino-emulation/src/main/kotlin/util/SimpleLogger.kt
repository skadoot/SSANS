package util

import java.util.logging.*

/**
 * Provides a simple logger.
 *
 * @author <a href="mailto:tyw1@aber.ac.uk">Tyler Lewis [tyw1@aber.ac.uk]</a>
 */
class SimpleLogger {
    companion object {
        private val logger = Logger.getLogger(SimpleLogger::class.java.name)

        init {
            logger.handlers.forEach { logger.removeHandler(it) }
            logger.addHandler(ConsoleHandler().apply {
                formatter = object : Formatter() {
                    override fun format(record: LogRecord): String {
                        return when (record.level) {
                            Level.WARNING -> "${record.level}: ${record.message}\n"
                            Level.SEVERE -> "${record.level}: ${record.message}\n"
                            else -> {
                                "${record.level}: ${record.message}\n"
                            }
                        }
                    }
                }
            })
            logger.useParentHandlers = false
        }

        fun setLevel(level: Level) {
            logger.level = level
        }

        fun info(message: String) {
            logger.info(message)
        }

        fun warning(message: String) {
            logger.warning(message)
        }

        fun severe(message: String) {
            logger.severe(message)
        }

        fun fine(message: String) {
            logger.fine(message)
        }

        fun finer(message: String) {
            logger.finer(message)
        }

        fun finest(message: String) {
            logger.finest(message)
        }
    }
}