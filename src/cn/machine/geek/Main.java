package cn.machine.geek;

import cn.machine.geek.structure.map.HashMap;


/**
 * @Author: MachineGeek
 * @Description:
 * @Email: 794763733@qq.com
 * @Date: 2020/12/2
 */
public class Main {
    public static void main(String[] args) {
        HashMap<Object,Integer> map = new HashMap<>();
        for (int i = 0; i < 20; i++){
            map.put(i,i * 10);
        }

        System.out.println(map.size());

        System.out.println(map.get(2));
    }
}
