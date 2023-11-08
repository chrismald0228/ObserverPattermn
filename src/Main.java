import java.util.*;

class Course{
    private String _cnum;
    private int _credits;
    public Course(){}
    public Course(String num, int cred){
        _cnum = num;  _credits = cred;
    }
    public void setNumber(String num){_cnum = num;}
    public void setCredits(int cred){_credits = cred;}
    public String getNumber(){return _cnum;}
    public int getCredits(){return _credits;}
    public String toString(){return _cnum + " " + _credits;}
}
class Student{
    private String _sid;
    public Student(){}
    public Student(String d){_sid = d;}
    public void setID(String id){_sid = id;}
    public String getID(){return _sid;}
    public String toString(){return _sid;}
}

interface Subject{
    void subscribe(Observer obs);
    void unsubscribe(Observer obs);
    void notifyObservers(String s);
}
interface Observer{
    void update(String s);
}
interface SearchBehavior<T, S> {
    // T is the object that contains the data
    // S is the search value
    boolean search(T obj, S v);
}
class StudentSearch implements SearchBehavior<Student, String>{
    @Override
    public boolean search(Student obj, String v) {
        return obj.getID().equals(v);
    }
}
class CourseSearch implements SearchBehavior<Course, String>{
    @Override
    public boolean search(Course obj, String v) {
        return obj.getNumber().equals(v);
    }
}
class CnumSearch implements SearchBehavior<String, String>{
    @Override
    public boolean search(String obj, String v) {
        return obj.equals(v);
    }
}

class AllItems<T> {
    private ArrayList<T> _items;
    public AllItems(){_items = new ArrayList<T>();}
    public void addItem(T t){_items.add(t);}
    public <S> int findItem(S v, SearchBehavior<T, S> sb){
        for (int i=0; i<_items.size(); i++){
            if (sb.search(_items.get(i), v)){
                return i;
            }
        }
        return -1;
    }
    public void removeItem(int i){
        if (i >= 0 && i < _items.size())
            _items.remove(i);
    }
    public int size(){return _items.size();}
    public T getItem(int i){return _items.get(i);}
}

class AllStudents implements Subject{
    private AllItems<Student> _students;
    private ArrayList<Observer> _observers;

    public AllStudents(){
        _students = new AllItems<Student>();
        _observers = new ArrayList<Observer>();
    }
    public void addStudent(String id){
        _students.addItem(new Student(id));
    }
    public boolean isStudent(String id){
        if (_students.findItem(id, new StudentSearch()) != -1)
            return true;
        else
            return false;
    }
    public int findStudent(String id){
        return _students.findItem(id, new StudentSearch());
    }
    public void removeStudent(String id){
        int i = findStudent(id);
        if(i!=-1) {
            _students.removeItem(i);
            notifyObservers(id);
        }
    }
    public boolean modifyStudentID(String oldID, String newID){
        int i = findStudent(oldID);
        if (i < 0)
            return false;
        else {
            _students.getItem(i).setID(newID);
            return true;
        }
    }
    public int size(){return _students.size();}
    public String toString(){
        String s = "Students:\n";
        for (int i=0; i<_students.size(); i++)
            s += (_students.getItem(i).toString() + "\n");
        return s;
    }

    public void subscribe(Observer obs){
        _observers.add(obs);
    }
    public void unsubscribe(Observer obs){
        _observers.remove(obs);
    }
    public void notifyObservers(String s){
        for(Observer observer : _observers){
            observer.update(s);
        }
    }
}
class Enrollment implements Observer{
    private HashMap<String, AllItems<String>> _enroll;
    private Subject _subject;

    public Enrollment(Subject s){
        _enroll = new HashMap<String, AllItems<String>>();
        _subject = s;
        s.subscribe(this);
    }
    public void update(String s){
        dropStudentFromAllCourses(s);
    }
    public void addCourseToStudent(String id, String c){
        AllItems<String> t = _enroll.get(id);
        if (t == null)  // student not in enroll
            t = new AllItems<String>();
        t.addItem(c);
        _enroll.put(id, t);
    }
    public void dropStudentFromAllCourses(String id){
        if (_enroll.containsKey(id))
            _enroll.remove(id);
    }
    public boolean dropStudentFromCourse(String id, String cnum){
        // Drops a student from a course
        // If student has no more courses then remove student from hashmap
        AllItems<String> t = _enroll.get(id);
        int i = t.findItem(cnum, new CnumSearch());
        if (i == -1)
            return false;
        t.removeItem(i);
        if (t.size() == 0)
            _enroll.remove(id);
        return true;
    }
    public boolean dropCourseFromAllStudents(String cnum){
        // Drops course from all students that are enrolled in the course
        // If student has no more courses then remove student from hashmap
        // Students that need to be removed will be stored in a temporary arraylist
        // in order to remove them from hashmap after iterating
        boolean found = false;
        // list of student ids to remove from hashmap
        ArrayList<String> kt = new ArrayList<String>();
        // extract keys from hashmap to iterator through
        Set keys = _enroll.keySet();
        Iterator itr = keys.iterator();
        while (itr.hasNext()) {
            found = false;
            String k = (String)itr.next();
            AllItems<String> t = _enroll.get(k);
            int i = t.findItem(cnum, new CnumSearch());
            if (i != -1) {
                t.removeItem(i);
                if (t.size() == 0)
                    kt.add(k);
                found = true;
            }
        }
        for (int i=0; i<kt.size(); i++)
            _enroll.remove(kt.get(i));

        return found;
    }
    public String toString(){
        String s = "Enrollment:\n";
        Set keys = _enroll.keySet();
        Iterator itr = keys.iterator();
        while (itr.hasNext()) {
            String k = (String)itr.next();
            AllItems<String> t = _enroll.get(k);
            s += (k + " ");
            for (int j=0; j<t.size(); j++)
                s += (t.getItem(j) + " ");
            s += "\n";
        }

        return s;
    }

}
public class Main {
    public static void main(String[] args) {
        AllStudents as = new AllStudents();
        Enrollment en = new Enrollment(as);

        as.addStudent("100");
        as.addStudent("200");
        as.addStudent("300");
        en.addCourseToStudent("100", "CSC1700");
        en.addCourseToStudent("100", "CSC2150");
        en.addCourseToStudent("100", "CSC2300");
        en.addCourseToStudent("200", "CSC1700");
        en.addCourseToStudent("200", "CSC2150");
        en.addCourseToStudent("200", "CSC2300");
        en.addCourseToStudent("200", "CSC3400");
        en.addCourseToStudent("300", "CSC1700");
        en.addCourseToStudent("300", "CSC2150");
        System.out.println(as);
        System.out.println(en);
        as.removeStudent("100");
        System.out.println(as);
        System.out.println(en);
    }
}
