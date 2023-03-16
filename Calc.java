import java.io.*;

//커밋 메시지 수정용 왜 그게 제목으로 커밋됐지
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
        int result = aexp();
        if (token == '\n') /* end the parse and print the result */
            System.out.println(result);
        else error();
    }

    Object expr() {
        /* expr -> bexp { '&' bexp  | '|' bexp } | '!' expr | true | false */
        Object result;

        if(token == '!'){ // ! expr
            match('!');
            result = !(boolean) expr();
        }
        else if(token == 't'){ //true
            String t_candidate = "t";
            try{
                char next = (char)input.read();
                while(Character.isAlphabetic(ch)){
                    t_candidate.concat(Character.toString(next));
                    next = (char)input.read();
                }
            } catch (IOException e) {
                System.err.println(e);
            }
            if(t_candidate.equals("true")){
                result = true;
                return result;
            }
        }

        else if(token == 'f'){ //false
            String f_candidate = "f";
            try{
                char next = (char)input.read();
                while(Character.isAlphabetic(ch)){
                    f_candidate.concat(Character.toString(next));
                    next = (char)input.read();
                }
            } catch (IOException e) {
                System.err.println(e);
            }
            if(f_candidate.equals("false")){
                result = false;
                return result;
            }
        }

        else{ // bexp {'&' bexp || '|' bexp }
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


    }

    Object relop(){
        // relop -> '==' | '!=' | '<' | '>' | '<=' | '>='

        int result = ch;

        try{
            ch = input.read();
            if (ch == '='){ // ==, !=, <=, >= 의 경우
                if (result == '='){
                    return "==";
                }
                else if(result == '!'){

                }
                else if (result == '<'){

                }
                else if(result == '>'){

                }
            }
            else{
                if (result == '<'){
                    return result;
                }
                else if(result == '>'){
                }

            }

        } catch(IOException e){
            System.err.println(e);
        }

        return result;
    }

    int aexp( ) {
        /* aexp -> term { '+' | '-' term ('-' 는 아직 구현 안함)} */
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
        return result;
    }

    int term( ) {
        /* term -> factor { '*' factor | '/' factor (나누기 구현 아직) } */
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
