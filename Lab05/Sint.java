package Lab05;
// Sint.java
// Interpreter for S
import java.util.Scanner;

public class Sint {
    static Scanner sc = new Scanner(System.in);
    static State state = new State();

    State Eval(Command c, State state) { 
        if (c instanceof Decl) {
            Decls decls = new Decls();
            decls.add((Decl) c);
            return allocate(decls, state);
        }

        if (c instanceof Function) {
            Function f = (Function) c;
            state.push(f.id, new Value(f));
            return state;
        }

        if (c instanceof Stmt){
            return Eval((Stmt) c, state);
        }
		
	    throw new IllegalArgumentException("no command");
    }
  
    State Eval(Stmt s, State state) {
        if (s instanceof Empty) 
	        return Eval((Empty)s, state);
        if (s instanceof Assignment)  
	        return Eval((Assignment)s, state);
        if (s instanceof If)  
	        return Eval((If)s, state);
        if (s instanceof While)  
	        return Eval((While)s, state);
        if (s instanceof Stmts)  
	        return Eval((Stmts)s, state);
	    if (s instanceof Let)  
	        return Eval((Let)s, state);
	    if (s instanceof Read)  
	        return Eval((Read)s, state);
	    if (s instanceof Print)  
	        return Eval((Print)s, state);
        if (s instanceof Call) 
	        return Eval((Call)s, state);
	    if (s instanceof Return) 
	        return Eval((Return)s, state);
        throw new IllegalArgumentException("no statement");
    }

    State Eval(Empty s, State state) {
        return state;
    }
  
    State Eval(Assignment a, State state) {
        if(a.ar != null){ //id[<expr>] = <expr>;
            Value v = V(a.expr, state);
            Value idx = V(a.ar.expr, state);
            Value[] arr = (state.get(a.ar.id)).arrValue();
            arr[idx.intValue()] = v;
            return state.set(a.ar.id, new Value(arr));
        }

        Value v = V(a.expr, state);
	    return state.set(a.id, v);
    }

    State Eval(Read r, State state) {
        if (r.id.type == Type.INT) {
	        int i = sc.nextInt();
	        state.set(r.id, new Value(i));
	    } 

	    if (r.id.type == Type.BOOL) {
	        boolean b = sc.nextBoolean();	
            state.set(r.id, new Value(b));
	    }

        if (r.id.type == Type.STRING) {
            String s = sc.next();
            state.set(r.id, new Value(s));
        }
	    return state;
    }

    State Eval(Print p, State state) {
	    System.out.println(V(p.expr, state));
        return state; 
    }
  
    State Eval(Stmts ss, State state) {
        for (Stmt s : ss.stmts) {
            state = Eval(s, state);
            if (s instanceof Return)  
                return state;
        }
        return state;
    }
  
    State Eval(If c, State state) {
        if (V(c.expr, state).boolValue( ))
            return Eval(c.stmt1, state);
        else
            return Eval(c.stmt2, state);
    }

    State Eval(While l, State state) {
        if (V(l.expr, state).boolValue( ))
            return Eval(l, Eval(l.stmt, state));
        else
            return state;
    }

    State Eval(Let l, State state) {
        State s = allocate(l.decls, state);
        s = Eval(l.stmts, s);
	    return free(l.decls, s);
    }

    State Eval(Call c, State state){
        // evaluate call without return value
        Value v = state.get(c.fid);
        Function f = v.funValue();
        State s = newFrame(state, c, f);
        s = Eval(f.stmt, s);
        s = deleteFrame(s, c, f);
        return null;
    }

    State Eval(Return r, State state){
        Value v = V(r.expr, state);
        return state.set(new Identifier("return"), v);
    }

    State allocate (Decls ds, State state) {
        if (ds != null){
            for(Decl d : ds){
                    if(d.expr == null){
                        if(d.arraysize > 0){ //<type> id[n]
                            state.push(d.id, new Value(new Value[d.arraysize]));
                        }
                        else{
                            state.push(d.id, new Value(0));
                        }
                    }
                    else{
                        state.push(d.id, V(d.expr, state));
                    }
            }
            return state;
        }
        return null;
    }

    State newFrame(State state, Call c, Function f){
        if(c.args.size() == 0)
            return state;

        Value val[] = new Value[f.params.size()];
        int i = 0;
        for (Expr e : c.args){
            val[i++] = V(e, state);
        }

        i = 0;
        for (Decl d : f.params){
            state.push(d.id, val[i++]);
        }
        state.push(new Identifier("return"), null);
        return state;
    }

    State free (Decls ds, State state) {
        if (ds != null){
            for (Decl d : ds){
                state.pop();
            }
        }
        return state;
    }

    State deleteFrame(State state, Call c, Function f){
        state.pop();
        state = free(f.params, state);
        return state;
    }

    Value binaryOperation(Operator op, Value v1, Value v2) {
        check(!v1.undef && !v2.undef,"reference to undef value");
	    switch (op.val) {
            case "+":
                return new Value(v1.intValue() + v2.intValue());
            case "-":
                return new Value(v1.intValue() - v2.intValue());
            case "*":
                return new Value(v1.intValue() * v2.intValue());
            case "/":
                return new Value(v1.intValue() / v2.intValue());

            case "==":
                if(v1.type == Type.INT && v2.type == Type.INT){
                    return new Value(v1.intValue() == v2.intValue());
                }
                else if(v1.type == Type.STRING && v2.type == Type.STRING){
                    return new Value(v1.stringValue().equals(v2.stringValue()));
                }

            case "<":
                if(v1.type == Type.INT && v2.type == Type.INT){
                    return new Value(v1.intValue() < v2.intValue());
                }
                else if(v1.type == Type.STRING && v2.type == Type.STRING){
                    if(v1.stringValue().compareTo(v2.stringValue()) < 0){
                        return new Value(true);
                    }
                    else{
                        return new Value(false);
                    }
                }

            case "<=":
                if(v1.type == Type.INT && v2.type == Type.INT){
                    return new Value(v1.intValue() <= v2.intValue());
                }
                else if(v1.type == Type.STRING && v2.type == Type.STRING){
                    if(v1.stringValue().compareTo(v2.stringValue()) <= 0){
                        return new Value(true);
                    }
                    else{
                        return new Value(false);
                    }
                }

            case ">":
                if(v1.type == Type.INT && v2.type == Type.INT){
                    return new Value(v1.intValue() > v2.intValue());
                }
                else if(v1.type == Type.STRING && v2.type == Type.STRING){
                    if(v1.stringValue().compareTo(v2.stringValue()) > 0){
                        return new Value(true);
                    }
                    else{
                        return new Value(false);
                    }
                }

            case ">=":
                if(v1.type == Type.INT && v2.type == Type.INT){
                    return new Value(v1.intValue() >= v2.intValue());
                }
                else if(v1.type == Type.STRING && v2.type == Type.STRING){
                    if(v1.stringValue().compareTo(v2.stringValue()) >= 0){
                        return new Value(true);
                    }
                    else{
                        return new Value(false);
                    }
                }

            case "&":
                if(v1.type == Type.BOOL && v2.type == Type.BOOL){
                    return new Value(v1.boolValue() && v2.boolValue());
                }

            case "|":
                if(v1.type == Type.BOOL && v2.type == Type.BOOL){
                    return new Value(v1.boolValue() || v2.boolValue());
                }
            default:
                throw new IllegalArgumentException("no operation");
	    }
    } 
    
    Value unaryOperation(Operator op, Value v) {
        check( !v.undef, "reference to undef value");
	    switch (op.val) {
        case "!": 
            return new Value(!v.boolValue( ));
        case "-": 
            return new Value(-v.intValue( ));
        default:
            throw new IllegalArgumentException("no operation: " + op.val); 
        }
    } 

    static void check(boolean test, String msg) {
        if (test) return;
        System.err.println(msg);
    }

    Value V(Call c, State state){
        Value v = state.get(c.fid);
        Function f = v.funValue();
        State s = newFrame(state, c, f);
        s = Eval(f.stmt, s);
        v = s.peek().val;
        s = deleteFrame(s, c, f);
        return v;
    }

    Value V(Expr e, State state) {
        if (e instanceof Value) 
            return (Value) e;

        if (e instanceof Identifier) {
	        Identifier v = (Identifier) e;
            return (Value)(state.get(v));
	    }

        if (e instanceof Binary) {
            Binary b = (Binary) e;
            Value v1 = V(b.expr1, state);
            Value v2 = V(b.expr2, state);
            return binaryOperation (b.op, v1, v2); 
        }

        if (e instanceof Unary) {
            Unary u = (Unary) e;
            Value v = V(u.expr, state);
            return unaryOperation(u.op, v); 
        }

        if (e instanceof Call) 
    	    return V((Call)e, state);

        if (e instanceof Array){
            //id[<expr>]
            Array a = (Array) e;
            Value v = V(a.expr, state);
            Value[] arr = (state.get(a.id)).arrValue();
            return arr[v.intValue()];

            // Value arr = (Value)(state.get(a.id));
            // Value[] a = arr.arrValue();
            //return a[v.intValue()];
        }

        throw new IllegalArgumentException("no operation");

    }

    public static void main(String args[]) {
	    if (args.length == 0) {
	        Sint sint = new Sint();
			Lexer.interactive = true;
            System.out.println("Language S Interpreter 2.0");
            System.out.print(">> ");
	        Parser parser  = new Parser(new Lexer());

	        do { // Program = Command*
	            if (parser.token == Token.EOF)
		            parser.token = parser.lexer.getToken();
	       
	            Command command=null;
                try {
	                command = parser.command();
                    // if (command != null)  command.display(0);    // display AST   
				    if (command == null) 
						 throw new Exception();
					 else  {
						 command.type = TypeChecker.Check(command); 
                         //System.out.println("\nType: "+ command.type);
					 }
                } catch (Exception e) {
                    System.out.println(e);
		            System.out.print(">> ");
                    continue;
                }

	            if (command.type != Type.ERROR) {
                    System.out.println("\nInterpreting..." );
                    try {
                        state = sint.Eval(command, state);
                    } catch (Exception e) {
                         System.err.println(e);  
                    }
                }
		    System.out.print(">> ");
	        } while (true);
	    }

        else {
	        System.out.println("Begin parsing... " + args[0]);
	        Command command = null;
	        Parser parser  = new Parser(new Lexer(args[0]));
	        Sint sint = new Sint();

	        do {	// Program = Command*
	            if (parser.token == Token.EOF)
                    break;

                try {
	                command = parser.command();
                    //if (command != null)  command.display(0);    // display AST
				    if (command == null){
                        throw new Exception();
                    }
                    else  {
                         // command.display(0);
						 command.type = TypeChecker.Check(command);
                         // System.out.println("\nType: "+ command.type);
					 }
                } catch (Exception e) {
                    System.out.println(e);
                    continue;
                }

	            if (command.type!=Type.ERROR) {
                    System.out.println("\nInterpreting..." + args[0]);
                    try {
                        state = sint.Eval(command, state);
                    } catch (Exception e) {
                        System.err.println(e);  
                    }
                }
	        } while (command != null);
        }        
    }
}