package cn.machine.geek;

import cn.machine.geek.structure.unionfind.UnionFindSize;


/**
 * @Author: MachineGeek
 * @Description:
 * @Email: 794763733@qq.com
 * @Date: 2020/12/2
 */
public class Main {
    public static void main(String[] args) {
        UnionFindSize unionFind = new UnionFindSize(12);
        unionFind.union(0,1);
        unionFind.union(0,2);
        unionFind.union(0,3);
        unionFind.union(0,4);
        unionFind.union(0,5);

        unionFind.union(6,7);

        unionFind.union(8,9);
        unionFind.union(8,10);
        unionFind.union(8,11);

        System.out.println(unionFind.isSame(0,6));
        System.out.println(unionFind.isSame(0,8));
        unionFind.union(0,8);
        System.out.println(unionFind.isSame(0,8));
        System.out.println(unionFind.find(0));
    }
}
