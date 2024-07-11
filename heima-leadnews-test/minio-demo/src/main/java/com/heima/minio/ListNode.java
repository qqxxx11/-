package com.heima.minio;

public class ListNode {
    int val;
    ListNode next;

    ListNode(int x) {
        val = x;
        next = null;
    }

    public static class Solution {
        public ListNode getIntersectionNode(ListNode headA, ListNode headB) {
            if (headB==null||headA==null){
                return null;
            }

            ListNode pa=headA;
            ListNode pb=headB;
            int al=0,bl=0;
            while (pa!=null){
                al++;
                pa=pa.next;
            }
            pa=headA;
            pb=headB;

            while (pb!=null){
                bl++;
                pb= pb.next;
            }
            if (al>bl){
                for (int i=0;i<al-bl;i++){
                    pa=pa.next;
                }
            }else if(al<bl){
                for (int i=0;i<bl-al;i++){
                    pb=pb.next;
                }
            }
            while (pa!=null){
                if (pa.val== pb.val){
                    return pa;
                }
                pa=pa.next;
                pb=pb.next;
            }
            return null;
        }

    }
}