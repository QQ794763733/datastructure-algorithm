package cn.machine.geek.structure.map;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @Author: MachineGeek
 * @Description: 映射（红黑树版本）
 * @Email: 794763733@qq.com
 * @Date: 2021/2/5
 */
public class TreeMap<K, V> {
    private Node<K, V> root;
    private int size;
    private Comparator<K> comparator;

    /**
     * @Author: MachineGeek
     * @Description: 红黑树节点
     * @Date: 2021/1/5
     * @Return:
     */
    public class Node<K, V> {
        private K key;
        private V value;
        private Node<K, V> parent;
        private Node<K, V> left;
        private Node<K, V> right;
        private boolean red = true;

        public Node(K key, V value, Node<K, V> parent) {
            this.key = key;
            this.value = value;
            this.parent = parent;
        }
    }

    /**
     * @Author: MachineGeek
     * @Description: 遍历操作抽象类
     * @Date: 2021/1/5
     * @Return:
     */
    public static abstract class Visitor<K, V> {
        boolean stop;

        protected abstract boolean operate(K key, V value);
    }

    /**
     * @param key
     * @Author: MachineGeek
     * @Description: 检查KEY
     * @Date: 2021/2/5
     * @Return: void
     */
    private void checkKey(K key) {
        if (key == null) {
            throw new RuntimeException("Key must not be null");
        }
    }

    /**
     * @param key
     * @Author: MachineGeek
     * @Description: 获取值
     * @Date: 2021/2/5
     * @Return: V
     */
    public V get(K key) {
        checkKey(key);
        Node<K, V> node = getNode(key);
        return node != null ? node.value : null;
    }

    /**
     * @param key
     * @param value
     * @Author: MachineGeek
     * @Description: 增加节点
     * @Date: 2021/2/5
     * @Return: void
     */
    public V put(K key, V value) {
        if (key == null) {
            throw new RuntimeException("Key must not be null");
        }
        // 根节点直接添加
        if (size == 0) {
            root = new Node<>(key, value, null);
            afterPut(root);
        } else {
            // 向左右查找并记录parent节点
            Node<K, V> temp = root;
            Node<K, V> parent = null;
            int flag = 0;
            while (temp != null) {
                parent = temp;
                flag = compare(temp.key, key);
                if (flag == 0) {
                    V old = temp.value;
                    temp.key = key;
                    temp.value = value;
                    return old;
                }
                if (flag > 0) {
                    temp = temp.left;
                } else {
                    temp = temp.right;
                }
            }
            // 使用节点元素与新增元素的比较值value值来判断放左边还是右边
            Node<K, V> node = new Node<>(key, value, parent);
            if (flag > 0) {
                parent.left = node;
            } else {
                parent.right = node;
            }
            // 新增后的处理
            afterPut(node);
        }
        size++;
        return null;
    }

    /**
     * @param node
     * @Author: MachineGeek
     * @Description: 红黑树添加节点后的处理
     * @Date: 2021/1/25
     * @Return: void
     */
    private void afterPut(Node<K, V> node) {
        // 如果是根节点直接染黑返回
        Node<K, V> parent = node.parent;
        if (parent == null) {
            node.red = false;
            return;
        }
        // 如果父节点是黑色不用处理，如果是红色则需要处理。
        if (parent.red) {
            Node<K, V> grandParent = parent.parent;
            Node<K, V> uncle = getBrother(parent);
            // 如果叔父节点是黑色，需要进行旋转操作，并设置颜色。
            if (isBlack(uncle)) {
                // L
                if (parent == parent.parent.left) {
                    // LL
                    if (node == parent.left) {
                        parent.red = false;
                        // LR
                    } else {
                        leftRotate(parent);
                        node.red = false;
                    }
                    grandParent.red = true;
                    rightRotate(grandParent);
                    // R
                } else {
                    // RL
                    if (node == parent.left) {
                        rightRotate(parent);
                        node.red = false;
                        // RR
                    } else {
                        parent.red = false;
                    }
                    grandParent.red = true;
                    leftRotate(grandParent);
                }
                // 如果叔父节点是红色，需要向上递归染色。
            } else {
                parent.red = false;
                uncle.red = false;
                grandParent.red = true;
                afterPut(grandParent);
            }
        }
    }

    /**
     * @param key
     * @Author: MachineGeek
     * @Description: 删除元素
     * @Date: 2021/2/5
     * @Return: void
     */
    public V remove(K key) {
        Node<K, V> node = getNode(key);
        // 节点为空直接返回
        if (node == null) {
            return null;
        }
        V old = node.value;
        // 如果节点左右子节点都不为空，寻找一个前驱节点赋值到自己，并让前驱结点删除。
        if (node.left != null && node.right != null) {
            Node<K, V> predecessor = predecessorNode(node);
            node.key = predecessor.key;
            node.value = predecessor.value;
            node = predecessor;
        }
        // 寻找这个将要被删除的节点的子节点作为替代节点
        Node<K, V> replace = node.left != null ? node.left : node.right;
        // 有子节点的情况处理
        if (replace != null) {
            replace.parent = node.parent;
            if (replace.parent == null) {
                root = replace;
            } else if (node.parent.left == node) {
                node.parent.left = replace;
            } else {
                node.parent.right = replace;
            }
            afterRemove(replace);
            // 根节点的情况处理
        } else if (node.parent == null) {
            root = null;
            afterRemove(node);
            // 无子节点的情况处理
        } else {
            if (node.parent.left == node) {
                node.parent.left = null;
            } else {
                node.parent.right = null;
            }
            afterRemove(node);
        }
        // 删除后的情况处理
        size--;
        return old;
    }

    /**
     * @param node
     * @Author: MachineGeek
     * @Description: 红黑树删除节点后的处理
     * @Date: 2021/2/1
     * @Return: void
     */
    private void afterRemove(Node<K, V> node) {
        // 如果删除的节点（或者替代节点）是红色，染黑后直接返回。
        if (isRed(node)) {
            node.red = false;
            return;
        }
        // 如果是根节点直接返回
        Node<K, V> parent = node.parent;
        if (parent == null) {
            return;
        }
        // 被删除的节点是黑色叶子节点，获取它的兄弟节点
        boolean left = parent.left == null || node == parent.left;
        Node<K, V> brother = left ? parent.right : parent.left;
        // 被删除的节点在左边，兄弟节点在右边
        if (left) {
            // 如果右边的兄弟节点为红色，则对父节点左旋，重新赋值兄弟节点。
            if (brother.red) {
                brother.red = false;
                parent.red = true;
                leftRotate(parent);
                brother = parent.right;
            }
            // 如果兄弟节点的左右节点都是黑色，则没有可借节点，父节点染黑，兄弟节点染红。
            if (isBlack(brother.left) && isBlack(brother.right)) {
                boolean red = parent.red;
                brother.red = true;
                parent.red = false;
                // 如果父节点是黑色，则会下溢，需要重新处理
                if (!red) {
                    afterRemove(parent);
                }
                // 兄弟节点至少有一个红色子节点
            } else {
                // 如果兄弟的右子节点是黑色，兄弟先进行一次右旋
                if (isBlack(brother.right)) {
                    rightRotate(brother);
                    brother = parent.right;
                }
                // 染色
                brother.red = parent.red;
                if (brother.right != null) {
                    brother.right.red = false;
                }
                parent.red = false;
                // 对父节点进行左旋转
                leftRotate(parent);
            }
            // 被删除的节点在右边，兄弟节点在左边
        } else {
            // 如果左边的兄弟节点为红色，则对父节点右旋，重新赋值兄弟节点。
            if (brother.red) {
                brother.red = false;
                parent.red = true;
                rightRotate(parent);
                brother = parent.left;
            }
            // 如果兄弟节点的左右节点都是黑色，则没有可借节点，父节点染黑，兄弟节点染红。
            if (isBlack(brother.left) && isBlack(brother.right)) {
                boolean red = parent.red;
                brother.red = true;
                parent.red = false;
                // 如果父节点是黑色，则会下溢，需要重新处理
                if (!red) {
                    afterRemove(parent);
                }
                // 兄弟节点至少有一个红色子节点
            } else {
                // 如果兄弟的左子节点是黑色，兄弟先进行一次左旋
                if (isBlack(brother.left)) {
                    leftRotate(brother);
                    brother = parent.left;
                }
                // 染色
                brother.red = parent.red;
                if (brother.left != null) {
                    brother.left.red = false;
                }
                parent.red = false;
                // 对父节点进行右旋转
                rightRotate(parent);
            }
        }
    }

    /**
     * @param
     * @Author: MachineGeek
     * @Description: 返回长度
     * @Date: 2021/2/5
     * @Return: int
     */
    public int size() {
        return size;
    }

    /**
     * @param
     * @Author: MachineGeek
     * @Description: 是否为空
     * @Date: 2021/2/5
     * @Return: boolean
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * @param
     * @Author: MachineGeek
     * @Description: 清空
     * @Date: 2021/2/5
     * @Return: void
     */
    public void clear() {
        root = null;
        size = 0;
    }

    /**
     * @param key
     * @Author: MachineGeek
     * @Description: 是否包含KEY
     * @Date: 2021/2/5
     * @Return: boolean
     */
    public boolean containsKey(K key) {
        return getNode(key) != null;
    }

    /**
     * @param value
     * @Author: MachineGeek
     * @Description: 是否包含VALUE
     * @Date: 2021/2/5
     * @Return: boolean
     */
    public boolean containsValue(V value) {
        if (root == null) {
            return false;
        }
        Queue<Node<K, V>> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            Node<K, V> node = queue.poll();
            if (valEquals(node.value, value)) {
                return true;
            }
            if (node.left != null) {
                queue.offer(node.left);
            }
            if (node.right != null) {
                queue.offer(node.right);
            }
        }
        return false;
    }

    /**
     * @param value1
     * @param value2
     * @Author: MachineGeek
     * @Description: 比较VALUE是否相等
     * @Date: 2021/2/5
     * @Return: boolean
     */
    public boolean valEquals(V value1, V value2) {
        return value1 == null ? value2 == null : value1.equals(value2);
    }

    /**
     * @param node
     * @Author: MachineGeek
     * @Description: 对节点进行左旋
     * @Date: 2020/12/31
     * @Return: cn.machine.geek.structure.tree.AVLTree<E>.Node<E>
     */
    private void leftRotate(Node<K, V> node) {
        Node<K, V> right = node.right;
        node.right = right.left;
        right.left = node;
        right.parent = node.parent;
        node.parent = right;
        if (node.right != null) {
            node.right.parent = node;
        }
        if (right.parent == null) {
            root = right;
        } else if (right.parent.left == node) {
            right.parent.left = right;
        } else {
            right.parent.right = right;
        }
    }

    /**
     * @param node
     * @Author: MachineGeek
     * @Description: 对节点进行右旋
     * @Date: 2020/12/31
     * @Return: cn.machine.geek.structure.tree.AVLTree<E>.Node<E>
     */
    private void rightRotate(Node<K, V> node) {
        Node<K, V> left = node.left;
        node.left = left.right;
        left.right = node;
        left.parent = node.parent;
        node.parent = left;
        if (node.left != null) {
            node.left.parent = node;
        }
        if (left.parent == null) {
            root = left;
        } else if (left.parent.left == node) {
            left.parent.left = left;
        } else {
            left.parent.right = left;
        }
    }

    /**
     * @param key
     * @Author: MachineGeek
     * @Description: 查找节点
     * @Date: 2021/1/5
     * @Return: cn.machine.geek.structure.tree.BinarySearchTree<E>.Node<E>
     */
    public Node<K, V> getNode(K key) {
        Node<K, V> temp = root;
        while (temp != null) {
            int value = compare(temp.key, key);
            if (value == 0) {
                return temp;
            }
            if (value > 0) {
                temp = temp.left;
            } else {
                temp = temp.right;
            }
        }
        return temp;
    }

    /**
     * @param node
     * @Author: MachineGeek
     * @Description: 找到当前节点的中序遍历前驱节点
     * @Date: 2021/1/5
     * @Return: cn.machine.geek.structure.tree.BinarySearchTree<E>.Node<E>
     */
    private Node<K, V> predecessorNode(Node<K, V> node) {
        if (node == null) {
            return null;
        }
        if (node.left != null) {
            node = node.left;
            while (node.right != null) {
                node = node.right;
            }
            return node;
        }
        while (node.parent != null && node.parent.left == node) {
            node = node.parent;
        }
        return node.parent;
    }

    /**
     * @param node
     * @Author: MachineGeek
     * @Description: 找到当前节点的中序遍历后继节点
     * @Date: 2021/1/5
     * @Return: cn.machine.geek.structure.tree.BinarySearchTree<E>.Node<E>
     */
    private Node<K, V> subsequentNode(Node<K, V> node) {
        if (node == null) {
            return null;
        }
        if (node.right != null) {
            node = node.right;
            while (node.left != null) {
                node = node.left;
            }
            return node;
        }
        while (node.parent != null && node.parent.right == node) {
            node = node.parent;
        }
        return node.parent;
    }

    /**
     * @param node
     * @Author: MachineGeek
     * @Description: 前序
     * @Date: 2021/1/5
     * @Return: void
     */
    private void preorder(Node<K, V> node, Visitor<K, V> visitor) {
        if (null == node || visitor.stop) {
            return;
        }
        visitor.stop = visitor.operate(node.key, node.value);
        preorder(node.left, visitor);
        preorder(node.right, visitor);
    }

    /**
     * @param node
     * @Author: MachineGeek
     * @Description: 中序
     * @Date: 2021/1/5
     * @Return: void
     */
    private void inorder(Node<K, V> node, Visitor<K, V> visitor) {
        if (null == node || visitor.stop) {
            return;
        }
        inorder(node.left, visitor);
        if (visitor.stop) {
            return;
        }
        visitor.stop = visitor.operate(node.key, node.value);
        inorder(node.right, visitor);
    }

    /**
     * @param node
     * @Author: MachineGeek
     * @Description: 后序
     * @Date: 2021/1/5
     * @Return: void
     */
    private void postorder(Node<K, V> node, Visitor<K, V> visitor) {
        if (null == node || visitor.stop) {
            return;
        }
        postorder(node.left, visitor);
        postorder(node.right, visitor);
        if (visitor.stop) {
            return;
        }
        visitor.stop = visitor.operate(node.key, node.value);
    }


    /**
     * @param node
     * @Author: MachineGeek
     * @Description: 判断节点是否为黑色
     * @Date: 2021/2/2
     * @Return: boolean
     */
    private boolean isBlack(Node<K, V> node) {
        return node == null || !node.red;
    }

    /**
     * @param node
     * @Author: MachineGeek
     * @Description: 判断节点是否为红色
     * @Date: 2021/2/2
     * @Return: boolean
     */
    private boolean isRed(Node<K, V> node) {
        return node != null && node.red;
    }


    /**
     * @param key1
     * @param key2
     * @Author: MachineGeek
     * @Description: 比较元素
     * @Date: 2021/1/5
     * @Return: int
     */
    private int compare(K key1, K key2) {
        if (comparator != null) {
            return comparator.compare(key1, key2);
        }
        return ((Comparable) key1).compareTo(key2);
    }

    /**
     * @param node
     * @Author: MachineGeek
     * @Description: 获取兄弟节点
     * @Date: 2021/1/25
     * @Return: cn.machine.geek.structure.tree.RedBlackTree<E>.Node<E>
     */
    private Node<K, V> getBrother(Node<K, V> node) {
        if (node == null || node.parent == null) {
            return null;
        } else if (node == node.parent.left) {
            return node.parent.right;
        } else {
            return node.parent.left;
        }
    }

    /**
     * @param visitor
     * @Author: MachineGeek
     * @Description: 遍历
     * @Date: 2021/2/5
     * @Return: void
     */
    public void traversal(Visitor<K, V> visitor) {
        inorder(root, visitor);
    }

}
