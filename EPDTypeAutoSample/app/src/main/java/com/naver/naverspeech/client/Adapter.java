package com.naver.naverspeech.client;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import android.os.Environment;

import java.io.File;

import android.app.DownloadManager;
import android.net.Uri;

import static com.naver.naverspeech.client.commSock.REQUEST_FILE;
import static com.naver.naverspeech.client.commSock.gson;
import static com.naver.naverspeech.client.commSock.read;

public class Adapter extends RecyclerView.Adapter<Adapter.ItemViewHolder> {


    // adapter에 들어갈 list 입니다.
    private ArrayList<Data> listData = new ArrayList<>();
    private Context context;
    // Item의 클릭 상태를 저장할 array 객체
    private SparseBooleanArray selectedItems = new SparseBooleanArray();
    // 직전에 클릭됐던 Item의 position
    private int prePosition = -1;

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // LayoutInflater를 이용하여 전 단계에서 만들었던 item.xml을 inflate 시킵니다.
        // return 인자는 ViewHolder 입니다.
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        // Item을 하나, 하나 보여주는(bind 되는) 함수입니다.
        holder.onBind(listData.get(position), position);
    }

    @Override
    public int getItemCount() {
        // RecyclerView의 총 개수 입니다.
        return listData.size();
    }

    void addItem(Data data) {
        // 외부에서 item을 추가시킬 함수입니다.
        listData.add(data);
    }

    // RecyclerView의 핵심인 ViewHolder 입니다.
    // 여기서 subView를 setting 해줍니다.
    class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView textView1;
        private TextView textView2;
        private TextView textView3;
        private TextView textView4;
        private ImageView imageView;

        private ImageButton export;
        private ImageButton detail;
        private Layout item;
        public Data data;
        private int position;


        ItemViewHolder(View itemView) {
            super(itemView);

            textView1 = itemView.findViewById(R.id.textView1);
            textView2 = itemView.findViewById(R.id.textView2);
            textView3 = itemView.findViewById(R.id.history_content);
            textView4 = itemView.findViewById(R.id.textView15);
            export = itemView.findViewById(R.id.export);
            detail = itemView.findViewById(R.id.detail);
            imageView = itemView.findViewById(R.id.imageView2);

        }

        void onBind(Data data, int position) {
            this.data = data;
            this.position = position;

            textView1.setText(this.data.getTitle());
            textView3.setText(this.data.getContent());
            textView4.setText(this.data.getMember());
            textView2.setText(this.data.getDate());
            changeVisibility(selectedItems.get(position));

            itemView.setOnClickListener(this);
            textView1.setOnClickListener(this);
            textView2.setOnClickListener(this);
            imageView.setOnClickListener(this);

            export.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                    final CustomDialog dialog = new CustomDialog(context, CustomDialog.EDITTEXT);
                    dialog.setTitleText("회의록 파일 저장");
                    dialog.setContentText("파일 이름을 지정해주세요");
                    dialog.setPositiveText("저장");
                    dialog.setNegativeText("취소");

                    dialog.setText(textView1.getText().toString());
                    dialog.setPositiveListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                commSock.kick(REQUEST_FILE, ItemViewHolder.this.data.getNumber());

                                String value = read();
                                SocketMessage receive = gson.fromJson(value, SocketMessage.class);

                                String fileURL = receive.message;

                                String Save_folder = "/seokEE";
                                String Save_Path = "";
                                String File_Name = dialog.getText();
                                String ext = Environment.getExternalStorageState();
                                if (ext.equals(Environment.MEDIA_MOUNTED)) {
                                    Save_Path = Environment.getExternalStorageDirectory()+ Save_folder;
                                }
                                File dir = new File(Save_Path);
                                if (!dir.exists()) {
                                    dir.mkdirs();
                                }
                                downloadFile(fileURL,Save_Path,File_Name);

                            }catch(Exception e){
                                e.printStackTrace();
                            }
                            dialog.dismiss();
                        }
                    });
                    dialog.setNegativeListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            });

            detail.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(context, resultActivity.class);
                    intent.putExtra("pincode", ItemViewHolder.this.data.getNumber());
                    context.startActivity(intent);
                }
            });
        }


        public void downloadFile(String furl,String fpath,String fname) {
            File direct = new File(fpath);

            if (!direct.exists()) {
                direct.mkdirs();
            }
            Uri uri = Uri.parse(furl);
            DownloadManager mgr = (DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE);

            DownloadManager.Request request = new DownloadManager.Request(uri);

            request.setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI
                            | DownloadManager.Request.NETWORK_MOBILE)
            .setDestinationInExternalPublicDir(direct+"/",fname+".docx")
            .setNotificationVisibility( DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);


            mgr.enqueue(request);

            // Open Download Manager to view File progress

//            context.startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));

        }



        @Override
        public void onClick(View v) {

            if (v.getId() == R.id.linearItem||v.getId() == R.id.textView1||v.getId() == R.id.textView2||v.getId() == R.id.imageView2) {
                if (selectedItems.get(position)) {
                    // 펼쳐진 Item을 클릭 시
                    selectedItems.delete(position);
                } else {
                    // 직전의 클릭됐던 Item의 클릭상태를 지움
                    selectedItems.delete(prePosition);
                    // 클릭한 Item의 position을 저장
                    selectedItems.put(position, true);
                }
                // 해당 포지션의 변화를 알림
                if (prePosition != -1) notifyItemChanged(prePosition);
                notifyItemChanged(position);
                // 클릭된 position 저장
                prePosition = position;
            }


        }

        /**
         * 클릭된 Item의 상태 변경
         *
         * @param isExpanded Item을 펼칠 것인지 여부
         */
        private void changeVisibility(final boolean isExpanded) {
            // height 값을 dp로 지정해서 넣고싶으면 아래 소스를 이용
            int dpValue = 100;
            float d = context.getResources().getDisplayMetrics().density;
            int height = (int) (dpValue * d);

            // ValueAnimator.ofInt(int... values)는 View가 변할 값을 지정, 인자는 int 배열
            ValueAnimator va = isExpanded ? ValueAnimator.ofInt(0, height) : ValueAnimator.ofInt(height, 0);
            // Animation이 실행되는 시간, n/1000초
            va.setDuration(300);
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    // value는 height 값
                    int value = (int) animation.getAnimatedValue();
                    // imageView의 높이 변경
                    textView3.getLayoutParams().height = 200;
                    textView3.requestLayout();
                    export.getLayoutParams().height = 120;
                    export.requestLayout();
                    detail.getLayoutParams().height = 120;
                    detail.requestLayout();

                    imageView.setImageResource(R.drawable.datil_unactive);
                    // textView3가 실제로 사라지게하는 부분
                    textView3.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
                    textView4.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
                    export.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
                    detail.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
                }
            });
            // Animation start
            va.start();
        }
    }
//
//    public class DownloadThread implements Runnable {
//
//        // 다운로드 받을 주소와 저장할 파일명을 위한 변수
//        String mAddr;
//        String mFile;
//
//        //생성자
//        public DownloadThread(String mAddr, String mFile) {
//            this.mAddr = mAddr;
//            this.mFile = mFile;
//
//        }
//
//        public void run() {
//            // 다운로드 받을 주소를 만들기 위한 변수
//            URL fUrl;
//            // 데이터를 바이트다윈로 읽을 때 읽을 위치를 저장할 변수
//            int read;
//            try {
//                // 다운로드 받을 URL 생성
//                fUrl = new URL(mAddr);
//                // 연결 객체 생성
//                HttpURLConnection conn = (HttpURLConnection) fUrl.openConnection();
////                conn.setRequestMethod("GET");
////                conn.setDoInput(true);
////                conn.connect();
//                // 연결된 파일의 크기 가져오기
//                int len = conn.getContentLength();
//                // 데이터를 저장할 배열생성
//                byte raster[] = new byte[len];
//                // 웹에서  읽어올 스트림 생성  - 일반 파일을 읽는 경우
//                InputStream is = conn.getInputStream();
//                // 파일에 기록할 스트림 생성
//                File file = new File(mFile);
//                FileOutputStream fos = new FileOutputStream(file);
//
//                while (true) {
//                    // is 에서  읽어서 raster에 젖ㅇ하고 읽은 마지막위치를 read에 ㅈ장
//                    read = is.read(raster);
//
//                    // 읽은데이터가 없다면
//                    if (read <= 0) {
//                        break;
//                    }
//                    // raster 배열에서 0부턴 read 만큼 읽어서 fos에 기록
//                    fos.write(raster, 0, read);
//                }
//                is.close();
//                fos.close();
//                conn.disconnect();
//
//
//            } catch (Exception e) {
//                System.out.println(e.getMessage());
//            }
//
//
//        }
//
//
//
//    }
//    private void showDownloadFile() {
//        Intent intent = new Intent();
//        intent.setAction(android.content.Intent.ACTION_VIEW);
//        File file = new File(Save_Path + "/" + File_Name);
//
//        // 파일 확장자 별로 mime type 지정해 준다.
//        if (File_extend.equals("txt")) {
//            intent.setDataAndType(Uri.fromFile(file), "text/*");
//        } else if (File_extend.equals("doc") || File_extend.equals("docx")) {
//            intent.setDataAndType(Uri.fromFile(file), "application/msword");
//        }
//        context.startActivity(intent);
//    }
}

