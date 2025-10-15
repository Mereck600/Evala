package evala;

import java.util.List;

interface EvalaCallable {
  int arity();
  Object call(Interpreter interpreter, List<Object> arguments);
}