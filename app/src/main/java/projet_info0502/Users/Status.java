package projet_info0502.Users;

import projet_info0502.Exceptions.MCQManagerException;

public enum Status {
    STUDENT("STUDENT"),
    TEACHER("TEACHER"),
    MIX("MIX");

    private String id;
    private Status(String id){
        this.id = id;
    }

    public static Status getStatus(String s){
        if(s.equals("STUDENT")){
            return Status.STUDENT;
        }else{
            if(s.equals("TEACHER")){
                return Status.TEACHER;
            }else{
                if(s.equals("MIX"))
                    return Status.MIX;
                else
                    throw new MCQManagerException("Trying to get a status that does not exist. String used: " + s);
            }
        }
    }
}
