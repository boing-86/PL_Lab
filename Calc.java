import java.io.*;

public class Calc {
    int token; int value; int ch;
    private PushbackInputStream input;
    final int NUMBER=256;

    Calc(PushbackInputStream is) {
        input = is;
    }

    int getToken( )  { /* tokens are characters */
        while(true) {
            try  {
                ch = input.read();
                System.out.println((char)ch);
                if (ch == ' ' || ch == '\t' || ch == '\r') ;
                else {
                    if (Character.isDigit(ch)) {
                        value = number( );
                        input.unread(ch);
                        return NUMBER;
                    }
                    else return ch;
                }
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }

    private int number( )  {
        /* number -> digit { digit } */
        int result = ch - '0';
        try  {
            ch = input.read();
            while (Character.isDigit(ch)) {
                result = 10 * result + ch -'0';
                ch = input.read();
            }
        } catch (IOException e) {
            System.err.println(e);
        }
        return result;
    }

    void error( ) {
        System.out.printf("parse error : %d\n", ch);
        //System.exit(1);
    }

    void match(int c) {
        if (token == c)
            token = getToken();
        else error();
    }

    void command( ) {
        /* command -> expr '\n' */
        Object result = expr();
        if (token == '\n') /* end the parse and print the result */
            System.out.println(result);
        else error();
    }

    Object expr() {
        /* expr -> bexp { '&' bexp  | '|' bexp } | '!' expr | true | false */
        Object result = new Object();

        if(token == '!'){ // ! expr
            System.out.print("############ !expr #############\n");
            match('!');
            result = !(boolean) expr();
        }

        else if(token == 't'){ //true
                result = true;
                return result;
        }

        else if(token == 'f'){ //false
                result = false;
                return result;

        }

        else{ // bexp {'&' bexp || '|' bexp }
            System.out.print("############ bexp {'&' bexp || '|' bexp } ############\n");
            result = bexp();

            while(token == '&' || token == '|'){
                if(token == '&'){
                    match('&');
                    result = (boolean)result && (boolean)bexp();
                }
                else {
                    match('|');
                    result = (boolean) result && (boolean) bexp();
                }
            }
        }

        return result;
    }

    Object bexp( ) {
        // bexp -> aexp [relop aexp]
        int result = aexp();
        String r_operator = relop();
        System.out.print("######### r_operand");
        System.out.print(r_operator);

        if(r_operator != null){
            int right = aexp();

            switch (r_operator){
                case "==":
                    System.out.print("\n" + result + " == " + right + "\n");
                    boolean a = result == right;
                    return a ;

                case "!=":
                    return result != right;

                case "<" :
                    return result < right;

                case "<=" :
                    return result <= right;

                case ">" :
                    return result > right;

                case ">=" :
                    return result >= right;
            }
        }

        return result;
    }

    String relop(){
        // relop -> '==' | '!=' | '<' | '>' | '<=' | '>='

        int result = ch;
        String r_result = null;

        switch (result){
            case '=':
                match('=');
                if (ch == '='){
                    r_result = "==";
                }
                break;

            case '!':
                match('=');
                if (ch == '='){
                    r_result = "!=";
                }
                break;

            case '<':
                match('<');
                if (ch == '='){
                    r_result = "<=";
                }
                else{
                    r_result = "<";
                }
                break;

            case '>':
                match('>');
                if (ch == '='){
                    r_result = ">=";
                }
                else{
                    r_result = ">";
                }
                break;
        }

        System.out.print("##### relop = " + r_result + "############ ");
        return r_result;
    }

    int aexp( ) {
        /* aexp -> term { '+' | '-' term } */
        int result = term();
        while (token == '+' || token == '-') {
            if(token == '+'){
                match('+');
                result += term();
            }
            else {
                match('-');
                result -= term();
            }
        }

        System.out.print("##### aexp = " + result + "############ ");
        return result;
    }

    int term( ) {
        /* term -> factor { '*' factor | '/' factor } */
        int result = factor();
        while (token == '*' || token == '/') {
            if(token == '*'){
                match('*');
                result *= factor();
            }
            else {
                match('/');
                result = result / factor();
            }
        }
        return result;
    }

    int factor() {
        /* factor -> '(' expr ')' | number */
        int result = 0;
        if (token == '(') {
            match('(');
            result = aexp();
            match(')');
        }
        else if (token == NUMBER) {
            result = value;
            match(NUMBER); //token = getToken();
        }
        return result;
    }

    void parse( ) {
        token = getToken(); // get the first token
        command();          // call the parsing command
        //System.out.print(relop());
    }

    public static void main(String args[]) {
        Calc calc = new Calc(new PushbackInputStream(System.in));

        while(true) {
            System.out.print(">> ");
            calc.parse();
        }
    }

}
